package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.utils.DurationToSecondsSerialized;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Data
@Builder
public class Film {
    private int id;
    @NotNull(message = "Поле имени не заданно")
    @NotBlank(message = "Имя состоит из пустой строки")
    private final String name;
    @Size(max = 200, message = "Размер описания не должен превышать 200 символов")
    private final String description;
    @PastOrPresent(message = "Дата создания фильма не может быть в будущем")
    private final LocalDate releaseDate;
    @JsonSerialize(using = DurationToSecondsSerialized.class)
    private final Duration duration;
}