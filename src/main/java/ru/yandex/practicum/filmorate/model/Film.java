package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.utils.DurationToSecondsSerialized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Data
@Builder
public class Film {
    private long id;
    @NotNull(message = "Поле имени не заданно")
    @NotBlank(message = "Имя состоит из пустой строки")
    private final String name;
    @Size(max = 200, message = "Размер описания не должен превышать 200 символов")
    private final String description;
    @PastOrPresent(message = "Дата создания фильма не может быть в будущем")

    private final LocalDate releaseDate;
    @JsonSerialize(using = DurationToSecondsSerialized.class)
    private final Duration duration;

    private final RatingMPA mpa;

    public void setNameMPA(String name) {
        this.mpa.setName(name);
    }

    private final List<Genre> genres = new ArrayList<>();

    public void setNameGenre(Genre genre) {
        for (Genre genreOfList : genres) {
            if (genreOfList.getId() == genre.getId()) {
                genreOfList.setName(genre.getName());
            }
        }
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public Integer getDurationInt() {
        return Math.toIntExact(this.duration.getSeconds());
    }

    public void deleteAllGenres() {
        genres.clear();
    }

    private final Set<Long> likes = new HashSet<>();

    public void addLike(long userId) {
        likes.add(userId);
    }

    public void deleteLike(long userId) {
        if (likes.contains(userId)) {
            likes.remove(userId);
        }
    }

    public Long getNumberOfLikes() {
        return (long) likes.size();
    }
}