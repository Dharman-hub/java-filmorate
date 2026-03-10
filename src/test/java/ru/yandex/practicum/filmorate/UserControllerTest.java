package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
    }


    @Test
    void userCreateTest() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .name("Oleg")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        User createUser = userController.create(user);

        assertNotNull(createUser.getId());
        assertEquals("neverGonnaGiveYouUp@gmail.com", createUser.getEmail());
        assertEquals("Rickroll", createUser.getLogin());
        assertEquals("Oleg", createUser.getName());
        assertEquals(LocalDate.of(2006, 9, 13), createUser.getBirthday());
    }

    @Test
    void shouldUseLoginWithoutName() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        User createUser = userController.create(user);

        assertEquals("Rickroll", createUser.getName());
    }

    @Test
    void shouldThrowValidationExceptionWithoutEmailSymbol() {
        User user = User.builder()
                .email("neverGonnaGiveYouUpgmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        ValidationException validationException = Assertions.assertThrows(ValidationException.class,
                () -> userController.create(user));

        assertEquals("Email должен содержать символ @", validationException.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenEmailIsBlank() {
        User user = User.builder()
                .email(" ")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        ValidationException validationException = Assertions.assertThrows(ValidationException.class,
                () -> userController.create(user));

        assertEquals("Email не может быть пустым", validationException.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenLoginIsBlank() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        ValidationException validationException = Assertions.assertThrows(ValidationException.class,
                () -> userController.create(user));

        assertEquals("Login не может быть пустым", validationException.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionWhenLoginContainsSpace() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rick roll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        ValidationException validationException = Assertions.assertThrows(ValidationException.class,
                () -> userController.create(user));

        assertEquals("Login не должен содержать пробелы", validationException.getMessage());
    }

    @Test
    void shouldThrowValidationExceptionBirthdayIsInFuture() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2100, 9, 13))
                .build();

        ValidationException validationException = Assertions.assertThrows(ValidationException.class,
                () -> userController.create(user));

        assertEquals("Дата рождения не может быть в будущем", validationException.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingUnknownUser() {
        User user = User.builder()
                .id(1L)
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowNotFoundExceptionWithoutId() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowNotFoundExceptionWithout() {
        User user = User.builder()
                .email("neverGonnaGiveYouUp@gmail.com")
                .login("Rickroll")
                .birthday(LocalDate.of(2006, 9, 13))
                .build();

        User createUser = userController.create(user);

        User updatedUser = User.builder()
                .id(createUser.getId())
                .email("practicum@gmail.com")
                .login("yandex")
                .name("company")
                .birthday(LocalDate.of(2000, 9, 18))
                .build();

        User result = userController.update(updatedUser);

        assertEquals(createUser.getId(), result.getId());
        assertEquals("practicum@gmail.com", result.getEmail());
        assertEquals("yandex", result.getLogin());
        assertEquals("company", result.getName());
        assertEquals(LocalDate.of(2000, 9, 18), result.getBirthday());
    }
}
