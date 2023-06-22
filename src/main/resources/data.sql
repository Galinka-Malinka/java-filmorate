MERGE INTO rating
  (rating_id, name, description)
  KEY(rating_id)
VALUES (1, 'G', 'у фильма нет возрастных ограничений'),
  (2, 'PG', 'детям рекомендуется смотреть фильм с родителями'),
  (3, 'PG-13', 'детям до 13 лет просмотр не желателен'),
  (4, 'R', 'лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),
  (5, 'NC-17', 'лицам до 18 лет просмотр запрещён');

MERGE INTO genre
  (genre_id, name)
  KEY(genre_id)
VALUES (1, 'Комедия'),
  (2, 'Драма'),
  (3, 'Мультфильм'),
  (4, 'Триллер'),
  (5, 'Документальный'),
  (6, 'Боевик');

MERGE INTO friendship_status
    (friendship_status_id, name)
    KEY(friendship_status_id)
VALUES (1, 'confirmed'),
       (2, 'unconfirmed');












