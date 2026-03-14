package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                """;
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, Set<Genre>> genresMap = getGenresForFilms(filmIds);
        Map<Long, Set<Long>> likesMap = getLikesForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {

        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);

        film.setGenres(getGenres(id));
        film.setLikes(getLikes(id));

        return Optional.of(film);
    }

    @Override
    public Film update(Film film) {

        String sql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        deleteGenres(film.getId());
        saveGenres(film);

        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film create(Film film) {
        try {
            String sql = """
                    INSERT INTO films (name, description, release_date, duration, mpa_id)
                    VALUES (?, ?, ?, ?, ?)
                    """;

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getMpa().getId());
                return ps;
            }, keyHolder);

            film.setId(keyHolder.getKey().longValue());
            saveGenres(film);

            return findById(film.getId()).orElseThrow();
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Указан несуществующий рейтинг MPA или жанр");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*, m.name AS mpa_name
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, count);

        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        Map<Long, Set<Genre>> genresMap = getGenresForFilms(filmIds);
        Map<Long, Set<Long>> likesMap = getLikesForFilms(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }

        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = """
                INSERT INTO likes (film_id, user_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;
        jdbcTemplate.update(sql, filmId, userId);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));

        film.setMpa(mpa);

        return film;
    }

    private Set<Genre> getGenres(Long filmId) {

        String sql = """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        return new LinkedHashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId));
    }

    private void saveGenres(Film film) {

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void deleteGenres(Long filmId) {

        String sql = """
                DELETE FROM film_genres
                WHERE film_id = ?
                """;

        jdbcTemplate.update(sql, filmId);
    }

    private Set<Long> getLikes(Long filmId) {

        String sql = """
                SELECT user_id
                FROM likes
                WHERE film_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("user_id"), filmId));
    }

    private Map<Long, Set<Genre>> getGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """.formatted(inSql);

        Map<Long, Set<Genre>> result = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");

            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));

            result.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }, filmIds.toArray());

        return result;
    }

    private Map<Long, Set<Long>> getLikesForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = """
                SELECT film_id, user_id
                FROM likes
                WHERE film_id IN (%s)
                """.formatted(inSql);

        Map<Long, Set<Long>> result = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");

            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }, filmIds.toArray());

        return result;
    }
}