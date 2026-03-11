package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
class FilmDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmDbStorage filmStorage;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void shouldCreateFilm() {
        Film film = Film.builder()
                .name("Matrix")
                .description("Sci-fi")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(new MpaRating(4, null))
                .genres(Set.of(new Genre(1, null), new Genre(2, null)))
                .build();

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();

        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(savedFilm -> {
                    assertThat(savedFilm.getName()).isEqualTo("Matrix");
                    assertThat(savedFilm.getDescription()).isEqualTo("Sci-fi");
                    assertThat(savedFilm.getDuration()).isEqualTo(136);
                    assertThat(savedFilm.getMpa().getId()).isEqualTo(4);
                    assertThat(savedFilm.getGenres()).hasSize(2);
                });
    }

    @Test
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Old Name")
                .description("Old Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(new MpaRating(1, null))
                .genres(Set.of(new Genre(1, null)))
                .build();

        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("New Name");
        createdFilm.setDescription("New Description");
        createdFilm.setDuration(120);
        createdFilm.setMpa(new MpaRating(3, null));
        createdFilm.setGenres(Set.of(new Genre(2, null), new Genre(3, null)));

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("New Name");
        assertThat(updatedFilm.getDescription()).isEqualTo("New Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(120);
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(3);
        assertThat(updatedFilm.getGenres()).hasSize(2);
    }

    @Test
    void shouldFindFilmById() {
        Film film = Film.builder()
                .name("Find Film")
                .description("Description")
                .releaseDate(LocalDate.of(2010, 10, 10))
                .duration(90)
                .mpa(new MpaRating(2, null))
                .genres(Set.of(new Genre(1, null)))
                .build();

        Film createdFilm = filmStorage.create(film);

        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm -> {
                    assertThat(foundFilm.getId()).isEqualTo(createdFilm.getId());
                    assertThat(foundFilm.getMpa().getId()).isEqualTo(2);
                    assertThat(foundFilm.getGenres()).hasSize(1);
                });
    }

    @Test
    void shouldFindAllFilms() {
        Film film1 = Film.builder()
                .name("Film 1")
                .description("Desc 1")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(100)
                .mpa(new MpaRating(1, null))
                .genres(Set.of(new Genre(1, null)))
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Desc 2")
                .releaseDate(LocalDate.of(2002, 2, 2))
                .duration(110)
                .mpa(new MpaRating(2, null))
                .genres(Set.of(new Genre(2, null)))
                .build();

        filmStorage.create(film1);
        filmStorage.create(film2);

        assertThat(filmStorage.findAll()).hasSize(2);
    }

    @Test
    void shouldAddLike() {
        Long userId = createUser(
                "user1@mail.ru",
                "user1",
                "User1",
                LocalDate.of(2000, 1, 1)
        );

        Film film = filmStorage.create(Film.builder()
                .name("Liked Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(100)
                .mpa(new MpaRating(1, null))
                .genres(Set.of(new Genre(1, null)))
                .build());

        filmStorage.addLike(film.getId(), userId);

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm ->
                        assertThat(foundFilm.getLikes()).contains(userId));
    }

    @Test
    void shouldRemoveLike() {
        Long userId = createUser(
                "user1@mail.ru",
                "user1",
                "User1",
                LocalDate.of(2000, 1, 1)
        );

        Film film = filmStorage.create(Film.builder()
                .name("Liked Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(100)
                .mpa(new MpaRating(1, null))
                .genres(Set.of(new Genre(1, null)))
                .build());

        filmStorage.addLike(film.getId(), userId);
        filmStorage.removeLike(film.getId(), userId);

        Optional<Film> filmOptional = filmStorage.findById(film.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(foundFilm ->
                        assertThat(foundFilm.getLikes()).isEmpty());
    }

    @Test
    void shouldReturnPopularFilms() {
        Long userId1 = createUser(
                "user1@mail.ru",
                "user1",
                "User1",
                LocalDate.of(2000, 1, 1)
        );

        Long userId2 = createUser(
                "user2@mail.ru",
                "user2",
                "User2",
                LocalDate.of(2000, 2, 2)
        );

        Long userId3 = createUser(
                "user3@mail.ru",
                "user3",
                "User3",
                LocalDate.of(2000, 3, 3)
        );

        Film film1 = filmStorage.create(Film.builder()
                .name("Popular Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(100)
                .mpa(new MpaRating(1, null))
                .genres(Set.of(new Genre(1, null)))
                .build());

        Film film2 = filmStorage.create(Film.builder()
                .name("Less Popular Film")
                .description("Desc")
                .releaseDate(LocalDate.of(2011, 1, 1))
                .duration(110)
                .mpa(new MpaRating(2, null))
                .genres(Set.of(new Genre(2, null)))
                .build());

        filmStorage.addLike(film1.getId(), userId1);
        filmStorage.addLike(film1.getId(), userId2);
        filmStorage.addLike(film2.getId(), userId3);

        List<Film> popularFilms = filmStorage.getPopularFilms(10);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popularFilms.get(1).getId()).isEqualTo(film2.getId());
    }


    private Long createUser(String email, String login, String name, LocalDate birthday) {
        User user = User.builder()
                .email(email)
                .login(login)
                .name(name)
                .birthday(birthday)
                .build();

        return userStorage.create(user).getId();
    }
}
