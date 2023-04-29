package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping  //добавление фильма
    public ResponseEntity<?> addFilm(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.addFilm(film);
    }

    @PutMapping  //обновление фильма
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.updateFilm(film);
    }

    @GetMapping  //получение всех фильмов
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @DeleteMapping  //удаление всех фильмов
    public void clearFilmMap() {
        filmService.clearFilmMap();
    }

    @GetMapping("/{id}")  //получение фильма по id
    public Film getFilmById(@PathVariable long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")  //добавление лайка
    public Film addLike(@PathVariable("id") long filmId, @PathVariable long userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")  //удаление лайка
    public Film deleteLike(@PathVariable("id") long filmId, @PathVariable long userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")  //вывод определённого колличества фильмов из рейтинга
    public List<Film> getRatingOfFilms(@RequestParam(value = "count",
            defaultValue = "10", required = false) Long count) throws ValidationException {
        return filmService.getRatingOfFilms(count);
    }
}
