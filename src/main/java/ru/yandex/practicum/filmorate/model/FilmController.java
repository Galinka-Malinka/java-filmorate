package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Exceptions.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class FilmController {

    private Map<Integer, Film> filmMap = new HashMap<>();
    private int id = 0;

    @PostMapping("/films")
    public ResponseEntity<?> addFilm(@Valid @RequestBody Film film) throws ValidationException {  //добавление фильма
        try {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895");
            }
            if (!film.getDuration().isPositive()) {
                throw new ValidationException("Продолжительность не может быть отрицательной");
            }
            film.setId(id + 1);
            id++;
            this.filmMap.put(film.getId(), film);
        } catch (ValidationException e) {
            log.warn(e.getMessage());
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
        log.debug("Добавление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping("/films")
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) throws ValidationException {  //обновление фильма
        try {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895");
            }
            if (!film.getDuration().isPositive()) {
                throw new ValidationException("Продолжительность не может быть отрицательной");
            }
            if (!filmMap.containsKey(film.getId())) {
                return new ResponseEntity<>(film, HttpStatus.NOT_FOUND);
            }
            this.filmMap.replace(film.getId(), film);
        } catch (ValidationException e) {
            log.warn(e.getMessage());
            return new ResponseEntity<>(film, HttpStatus.BAD_REQUEST);
        }
        log.debug("Обновление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @GetMapping("/films")
    public List<Film> getAllFilms() {  //получение всех фильмов
        return new ArrayList<>(filmMap.values());
    }

    @DeleteMapping("/films")
    public void clearFilmMap() {
        filmMap.clear();
        id = 0;
        log.debug("Удаление всех фильмов");
    }
}
