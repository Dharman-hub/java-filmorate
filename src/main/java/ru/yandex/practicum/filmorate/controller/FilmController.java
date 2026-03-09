package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);

        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.warn("Ошибка создания фильма: пустое описание");
            throw new ValidationException("Описание не может быть пустым");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Добавлен фильм c id {}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {

        if (film.getId() == null) {
            log.warn("Ошибка обновления фильма: не указан id");
            throw new NotFoundException("id не может быть пустым");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Ошибка обновления фильма: фильм с таким id не найден");
            throw new NotFoundException("Фильм с таким id не найден");
        }

        validateFilm(film);

        films.put(film.getId(), film);
        return film;
    }

    private void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание не соответствует требованиям");
            throw new ValidationException("Описание фильма не может быть больше 200 символов");
        }

        if (film.getDuration() < 1) {
            log.warn("Валидация не пройдена: продолжительность не положительное число");
            throw new ValidationException("Продолжительность фильма не может быть отрицательным числом");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.warn("Валидация не пройдена: дата релиза раньше {}", CINEMA_BIRTHDAY);
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
