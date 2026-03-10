package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        filmController = new FilmController();
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
