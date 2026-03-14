package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<MpaRating> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Optional<MpaRating> findById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, this::mapRowToMpa, id);

        if (ratings.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ratings.get(0));
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        MpaRating rating = new MpaRating();
        rating.setId(rs.getInt("id"));
        rating.setName(rs.getString("name"));
        return rating;
    }
}