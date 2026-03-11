# java-filmorate
Template repository for Filmorate project.

## ER-диаграмма базы данных

![Er-диаграмма](ER-диаграмма.png)

## Описание схемы базы данных

База данных Filmorate состоит из следующих таблиц:

### users
Хранит информацию о пользователях.

Поля:
- id — идентификатор пользователя (PK)
- email — электронная почта
- login — логин пользователя
- name — имя
- birthday — дата рождения

### films
Хранит информацию о фильмах.

Поля:
- id — идентификатор фильма (PK)
- name — название
- description — описание
- release_date — дата выхода
- duration — продолжительность
- mpa_id — ссылка на рейтинг MPA (FK)

### mpa_ratings
Справочник возрастных рейтингов.

Поля:
- id — идентификатор рейтинга (PK)
- name — название рейтинга (G, PG, PG-13, R, NC-17)

### genres
Справочник жанров фильмов.

Поля:
- id — идентификатор жанра (PK)
- name — название жанра

### film_genres
Связующая таблица между фильмами и жанрами (many-to-many).

Поля:
- film_id — идентификатор фильма (FK)
- genre_id — идентификатор жанра (FK)

### likes
Хранит информацию о лайках пользователей фильмам.

Поля:
- film_id — идентификатор фильма (FK)
- user_id — идентификатор пользователя (FK)

### friendships
Хранит информацию о дружбе между пользователями.

Поля:
- user_id — пользователь, отправивший запрос
- friend_id — пользователь, получивший запрос
- status — статус дружбы (UNCONFIRMED / CONFIRMED)


## Примеры основных запросов

### Получить всех пользователей
SELECT *<br>
FROM users;

### Получить все фильмы
SELECT * <br>
FROM films;

### Получить фильм с его жанрами
SELECT f.name, g.name<br>
FROM films f<br>
JOIN film_genres fg ON f.id = fg.film_id<br>
JOIN genres g ON fg.genre_id = g.id<br>
WHERE f.id = 1;

### Получить топ 10 популярных фильмов
SELECT f.*, COUNT(l.user_id) AS likes_count<br>
FROM films f<br>
LEFT JOIN likes l ON f.id = l.film_id<br>
GROUP BY f.id<br>
ORDER BY likes_count DESC<br>
LIMIT 10;

### Получить друзей пользователя
SELECT u.*<br>
FROM users u<br>
JOIN friendships f ON u.id = f.friend_id<br>
WHERE f.user_id = 1<br>
AND f.status = 'CONFIRMED';