package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
public class InMemoryFilmStorage implements FilmStorage {  //добавление фильма

    private final Map<Long, Film> filmMap = new HashMap<>();
    private int id = 0;

    private final Set<Long> ratingOfFilms = new HashSet<>();

    public static Map<Integer, String> ratingMPAMap;

    static {
        ratingMPAMap = new HashMap<>();
        ratingMPAMap.put(1, "G");
        ratingMPAMap.put(2, "PG");
        ratingMPAMap.put(3, "PG-13");
        ratingMPAMap.put(4, "R");
        ratingMPAMap.put(5, "NC-17");
    }

    public static Map<Integer, String> genreMap;

    static {
        genreMap = new HashMap<>();
        genreMap.put(1, "Комедия");
        genreMap.put(2, "Драма");
        genreMap.put(3, "Мультфильм");
        genreMap.put(4, "Триллер");
        genreMap.put(5, "Документальный");
        genreMap.put(6, "Боевик");
    }

    private final UserStorage userStorage;

    public InMemoryFilmStorage(InMemoryUserStorage storage) {
        this.userStorage = storage;
    }

    @Override
    public ResponseEntity<?> addFilm(Film film) throws ValidationException {  //добавление фильма
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895");
        }
        if (film.getDuration().isNegative()) {
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
        film.setId(id + 1);
        id++;
        film.setNameMPA(ratingMPAMap.get(film.getMpa().getId()));
        Set<Genre> genresSet = new LinkedHashSet<>(film.getGenres());
        film.deleteAllGenres();
        for (Genre genre : genresSet) {
            genre.setName(genreMap.get(genre.getId()));
            film.addGenre(genre);
        }

        this.filmMap.put(film.getId(), film);
        ratingOfFilms.add(film.getId());

        log.debug("Добавление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateFilm(Film film) throws ValidationException {  //обновление фильма
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата создания фильма не может быть раньше 28.12.1895");
        }
        if (film.getDuration().isNegative()) {
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
        if (!filmMap.containsKey(film.getId())) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }

        film.setNameMPA(ratingMPAMap.get(film.getMpa().getId()));
        Set<Genre> genresSet = new LinkedHashSet<>(film.getGenres());
        film.deleteAllGenres();

        for (Genre genre : genresSet) {
            genre.setName(genreMap.get(genre.getId()));
            film.addGenre(genre);
        }
        this.filmMap.replace(film.getId(), film);

        log.debug("Обновление фильма: {}", film.getName());
        return new ResponseEntity<>(film, HttpStatus.OK);
    }

    @Override
    public List<Film> getAllFilms() { //получение всех фильмов
        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film getFilmById(long filmId) {  //получение фильма по id
        if (!filmMap.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }
        return filmMap.get(filmId);
    }

    @Override
    public void clearFilmMap() {  //удаление всех фильмов
        filmMap.clear();
        id = 0;
        ratingOfFilms.clear();
        log.debug("Удаление всех фильмов");
    }

    @Override
    public Film addLike(long filmId, long userId) { //добавление лайка
        if (!filmMap.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильма с id = " + filmId + " не существует");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователя с id = " + userId + " не существует");
        }
        filmMap.get(filmId).addLike(userId);
        ratingOfFilms.add(filmId);
        return filmMap.get(filmId);
    }

    @Override
    public Film deleteLike(long filmId, long userId) {  //удаление лайка
        if (!filmMap.containsKey(filmId)) {
            throw new FilmNotFoundException("Фильма с таким id не существует");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new UserNotFoundException("Пользователя с id = " + userId + " не существует");
        }
        filmMap.get(filmId).deleteLike(userId);
        return filmMap.get(filmId);

    }

    @Override
    public List<Film> getRatingOfFilms(Long number) throws ValidationException {  //вывод определённого колличества
        // фильмов из рейтинга
        if (number < 0) {
            throw new ValidationException("Параметр count не может быть отрицательным");
        }
        List<Long> listIdRating = ratingOfFilms.stream().sorted((p0, p1) -> {
            filmMap.get(p0).getNumberOfLikes();
            int compare = filmMap.get(p0).getNumberOfLikes().compareTo(filmMap.get(p1).getNumberOfLikes());
            return -1 * compare;
        }).limit(number).collect(Collectors.toList());

        List<Film> listFilmRating = new ArrayList<>();
        for (long id : listIdRating) {
            listFilmRating.add(filmMap.get(id));
        }
        return listFilmRating;
    }


    @Override  //получение рейтинга mpa по id
    public RatingMPA getRatingMPAById(Integer id) {
        if (!ratingMPAMap.containsKey(id)) {
            throw new MPANotFoundException("mpa с таким id не существует");
        }
        return RatingMPA.builder()
                .id(id)
                .name(ratingMPAMap.get(id))
                .build();
    }

    @Override  //вывод всех рейтингов mpa
    public List<RatingMPA> getAllRatingMPA() {
        List<RatingMPA> ratingMPAList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ratingMPAList.add(RatingMPA.builder().id(i).name(ratingMPAMap.get(i)).build());
        }
        return ratingMPAList;
    }

    @Override  //получение жанра по id
    public Genre getGenreById(Integer id) {
        if (!genreMap.containsKey(id)) {
            throw new GenreNotFoundException("жанра с таким id не существует");
        }
        return Genre.builder()
                .id(id)
                .name(genreMap.get(id))
                .build();
    }

    @Override  //вывод всех жанров
    public List<Genre> getAllGenre() {
        List<Genre> genreList = new ArrayList<>();
        for (int i = 1; i <= genreMap.size(); i++) {
            genreList.add(Genre.builder().id(i).name(genreMap.get(i)).build());
        }
        return genreList;
    }
}
