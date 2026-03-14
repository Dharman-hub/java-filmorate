package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User friend = findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        user.getFriends().add(friend.getId());
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        user.getFriends().remove(friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        return user.getFriends().stream()
                .map(friendId -> findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден")))
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        User otherUser = findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + otherUserId + " не найден"));

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(friendId -> findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден")))
                .toList();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
