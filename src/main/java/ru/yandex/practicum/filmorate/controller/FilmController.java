package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен список всех фильмов");
        return filmStorage.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Добавлен фильм c id {}", createdFilm.getId());
        return createdFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film.getId() == null) {
            throw new ValidationException("Id фильма должен быть указан");
        }

        if (filmStorage.findById(film.getId()) == null) {
            log.warn("Ошибка обновления фильма: фильм с таким id не найден");
            throw new NotFoundException("Фильм с таким id не найден");
        }

        validateFilm(film);
        return filmStorage.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрошен список популярных фильмов, count={}", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрошен фильм с id {}", id);

        Film film = filmStorage.findById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

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
}
