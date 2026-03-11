package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен список всех пользователей");
        return userService.findAll();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        setDefaultUserName(user);
        User createdUser = userService.create(user);
        log.info("Создан пользователь с id {}", createdUser.getId());
        return createdUser;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: id не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (userService.findById(newUser.getId()) == null) {
            log.warn("Пользователь с id {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id " + newUser.getId() + " не найден");
        }

        validateUser(newUser);
        setDefaultUserName(newUser);

        return userService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Запрошен список друзей пользователя {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Запрошены общие друзья пользователей {} и {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрошен пользователь с id {}", id);

        User user = userService.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email пустой");
            throw new ValidationException("Email не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email без @");
            throw new ValidationException("Email должен содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: login пустой");
            throw new ValidationException("Login не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: login содержит пробел");
            throw new ValidationException("Login не должен содержать пробелы");
        }

        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена: birthday пустой");
            throw new ValidationException("Дата рождения не может быть пустой");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: birthday в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void setDefaultUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
