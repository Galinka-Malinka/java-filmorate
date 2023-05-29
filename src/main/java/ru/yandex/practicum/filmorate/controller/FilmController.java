package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequestMapping
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping("/films")  //добавление фильма
    public ResponseEntity<?> addFilm(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.addFilm(film);
    }

    @PutMapping("/films")  //обновление фильма
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) throws ValidationException {
        return filmService.updateFilm(film);
    }

    @GetMapping("/films")  //получение всех фильмов
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @DeleteMapping("/films")  //удаление всех фильмов
    public void clearFilmMap() {
        filmService.clearFilmMap();
    }

    @GetMapping("/films/{id}")  //получение фильма по id
    public Film getFilmById(@PathVariable long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/films/{id}/like/{userId}")  //добавление лайка
    public Film addLike(@PathVariable("id") long filmId, @PathVariable long userId) {
        return filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")  //удаление лайка
    public Film deleteLike(@PathVariable("id") long filmId, @PathVariable long userId) {
        return filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/films/popular")  //вывод определённого колличества фильмов из рейтинга
    public List<Film> getRatingOfFilms(@RequestParam(value = "count",
            defaultValue = "10", required = false) Long count) throws ValidationException {
        return filmService.getRatingOfFilms(count);
    }

    @GetMapping("/mpa")  //вывод всех рейтингов mpa
    public List<RatingMPA> getAllRatingMPA() {
        return filmService.getAllRatingMPA();
    }

    @GetMapping("/mpa/{id}")  //получение рейтинга mpa по id
    public RatingMPA getRatingMPAById(@PathVariable Integer id) {
        return filmService.getRatingMPAById(id);
    }

    @GetMapping("/genres")  //вывод всех жанров
    public List<Genre> getAllGenre() {
        return filmService.getAllGenre();
    }

    @GetMapping("/genres/{id}")  //получение жанра по id
    public Genre getGenreById(@PathVariable Integer id) {
        return filmService.getGenreById(id);
    }
}
