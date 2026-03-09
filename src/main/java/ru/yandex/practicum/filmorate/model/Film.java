package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * Film.
 */
@Builder
@Data
public class Film {
    private String name;
    private Long id;
    private String description;
    private LocalDate releaseDate;
    private int duration ;

}
