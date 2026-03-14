package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
class UserDbStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void shouldCreateUser() {
        User user = User.builder()
                .email("oleg@mail.ru")
                .login("oleg_login")
                .name("Олег")
                .birthday(LocalDate.of(2007, 9, 13))
                .build();

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();

        Optional<User> userOptional = userStorage.findById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(savedUser -> {
                    assertThat(savedUser.getEmail()).isEqualTo("oleg@mail.ru");
                    assertThat(savedUser.getLogin()).isEqualTo("oleg_login");
                    assertThat(savedUser.getName()).isEqualTo("Олег");
                    assertThat(savedUser.getBirthday()).isEqualTo(LocalDate.of(2007, 9, 13));
                });
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("test_login")
                .name("Test")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User createdUser = userStorage.create(user);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.ru");

        userStorage.update(createdUser);

        Optional<User> updatedUserOptional = userStorage.findById(createdUser.getId());

        assertThat(updatedUserOptional)
                .isPresent()
                .hasValueSatisfying(updatedUser -> {
                    assertThat(updatedUser.getName()).isEqualTo("Updated Name");
                    assertThat(updatedUser.getEmail()).isEqualTo("updated@mail.ru");
                });
    }

    @Test
    void shouldFindUserById() {
        User user = User.builder()
                .email("find@mail.ru")
                .login("find_login")
                .name("Find User")
                .birthday(LocalDate.of(1999, 5, 10))
                .build();

        User createdUser = userStorage.create(user);

        Optional<User> userOptional = userStorage.findById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(foundUser ->
                        assertThat(foundUser.getId()).isEqualTo(createdUser.getId()));
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(2001, 2, 2))
                .build();

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldAddFriend() {
        User user = userStorage.create(User.builder()
                .email("user@mail.ru")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());

        User friend = userStorage.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(2001, 1, 1))
                .build());

        userStorage.addFriend(user.getId(), friend.getId());

        Collection<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .contains(friend.getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user = userStorage.create(User.builder()
                .email("user@mail.ru")
                .login("user")
                .name("User")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());

        User friend = userStorage.create(User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .name("Friend")
                .birthday(LocalDate.of(2001, 1, 1))
                .build());

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.removeFriend(user.getId(), friend.getId());

        Collection<User> friends = userStorage.getFriends(user.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = userStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("user1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .build());

        User user2 = userStorage.create(User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .name("User2")
                .birthday(LocalDate.of(2000, 2, 2))
                .build());

        User commonFriend = userStorage.create(User.builder()
                .email("common@mail.ru")
                .login("common")
                .name("Common")
                .birthday(LocalDate.of(2000, 3, 3))
                .build());

        User otherFriend = userStorage.create(User.builder()
                .email("other@mail.ru")
                .login("other")
                .name("Other")
                .birthday(LocalDate.of(2000, 4, 4))
                .build());

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user1.getId(), otherFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        Collection<User> commonFriends =
                userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }
}
