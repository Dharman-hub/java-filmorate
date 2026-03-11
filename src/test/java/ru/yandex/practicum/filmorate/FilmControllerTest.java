package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);

        filmController = new FilmController(filmService);
    }


    @Test
    void createFilmTest() {
        Film film = Film.builder()
                .name("Interstellar")
                .description("text")
                .duration(169)
                .releaseDate(LocalDate.of(2014, 10, 26))
                .build();

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm.getId());
        assertEquals("Interstellar", createdFilm.getName());
        assertEquals("text", createdFilm.getDescription());
        assertEquals(LocalDate.of(2014, 10, 26), createdFilm.getReleaseDate());
        assertEquals(169, createdFilm.getDuration());
        assertEquals(1, filmController.findAll().size());
    }

    @Test
    void shouldThrowValidationExceptionWhenNameIsBlank() {
        Film film = Film.builder()
                .name(" ")
                .description("text")
                .duration(169)
                .releaseDate(LocalDate.of(2014, 10, 26))
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowValidationExceptionWhenDescriptionIsTooLong() {
        Film film = Film.builder()
                .name("Interstellar")
                .description("qq".repeat(101))
                .duration(169)
                .releaseDate(LocalDate.of(2014, 10, 26))
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowValidationExceptionWhenDurationIsNotPositive() {
        Film film = Film.builder()
                .name("Interstellar")
                .description("qq")
                .duration(0)
                .releaseDate(LocalDate.of(2014, 10, 26))
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void shouldThrowValidationExceptionWhenReleaseDateIsBeforeCinemaBirthday() {
        Film film = Film.builder()
                .name("Interstellar")
                .description("qq")
                .duration(169)
                .releaseDate(LocalDate.of(1014, 10, 26))
                .build();

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }
}
