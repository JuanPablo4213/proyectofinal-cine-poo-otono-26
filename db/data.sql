-- =====================================================================
--  Datos de prueba: géneros, usuarios, cines, salas, asientos,
--  algunas películas y funciones para poder probar la compra.
-- =====================================================================
USE cinemex;

-- Usuarios de prueba (contraseñas con SHA-256 + sal)
--   admin@cinemex.mx   / admin123     (Administrador)
--   cliente@cinemex.mx / cliente123   (Cliente)
INSERT INTO usuarios (nombre, email, password_hash, sal, rol, telefono, num_empleado) VALUES
('Administrador', 'admin@cinemex.mx',
 '3b744480b6785d59d15719a0bbf2102b331ba9e0e03a478209f4758ab320edc3', 'c1n3m3x5al7', 'ADMINISTRADOR', NULL, 'EMP-001'),
('Cliente de prueba', 'cliente@cinemex.mx',
 '88d6985ca26d509aaaa6de353c4c8d122b3452c9af6aa24964b649fb1bd9cebc', 's4lCl13nt3', 'CLIENTE', '5512345678', NULL);

-- Catálogo de géneros (mismos ids que TMDB; la app los vuelve a sincronizar con /3/genre/movie/list)
INSERT INTO generos (id, nombre) VALUES
(28,'Acción'),(12,'Aventura'),(16,'Animación'),(35,'Comedia'),(80,'Crimen'),
(99,'Documental'),(18,'Drama'),(10751,'Familia'),(14,'Fantasía'),(36,'Historia'),
(27,'Terror'),(10402,'Música'),(9648,'Misterio'),(10749,'Romance'),(878,'Ciencia ficción'),
(10770,'Película de TV'),(53,'Suspense'),(10752,'Bélica'),(37,'Western');

-- Cines
INSERT INTO cines (id, nombre, ciudad, direccion) VALUES
(1, 'Cinemex Antara',      'Ciudad de México', 'Av. Ejército Nacional 843, Polanco'),
(2, 'Cinemex Santa Fe',    'Ciudad de México', 'Av. Vasco de Quiroga 3800, Santa Fe'),
(3, 'Cinemex Galerías',    'Guadalajara',      'Av. Rafael Sanzio 150, Zapopan');

-- Salas (tipo = subclase de POJOSala)
INSERT INTO salas (id, cine_id, numero, tipo, filas, columnas, cargo_efectos) VALUES
(1, 1, 1, 'TRADICIONAL', 8, 12, 0),
(2, 1, 2, 'PLATINO',     5,  8, 0),
(3, 1, 3, 'X4D',         6, 10, 35),
(4, 2, 1, 'TRADICIONAL', 8, 12, 0),
(5, 2, 2, 'PLATINO',     5,  8, 0),
(6, 3, 1, 'TRADICIONAL', 7, 10, 0);

-- Asientos: se generan todos los de cada sala (filas A, B, C... x columnas 1..N)
INSERT INTO asientos (sala_id, fila, numero)
WITH RECURSIVE
  f AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM f WHERE n < 26),
  c AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM c WHERE n < 30)
SELECT s.id, CHAR(64 + f.n), c.n
FROM salas s
JOIN f ON f.n <= s.filas
JOIN c ON c.n <= s.columnas
ORDER BY s.id, f.n, c.n;

-- Películas de ejemplo (tmdb_id reales). El póster y los demás datos se completan
-- desde TMDB cuando el administrador abre la película y presiona "Actualizar desde TMDB".
INSERT INTO peliculas (id, tmdb_id, titulo, sinopsis, duracion_min, clasificacion, poster_path, fecha_estreno, en_cartelera) VALUES
(1, 693134,  'Dune: Parte dos', 'Paul Atreides se une a los Fremen y busca venganza contra quienes destruyeron a su familia.', 166, 'B',  NULL, '2024-02-29', TRUE),
(2, 1022789, 'IntensaMente 2',  'Riley entra a la adolescencia y nuevas emociones llegan al cuartel general.',                96, 'AA', NULL, '2024-06-13', TRUE);

INSERT INTO pelicula_genero (pelicula_id, genero_id) VALUES
(1, 878), (1, 12), (2, 16), (2, 10751), (2, 35);

-- Funciones de los próximos días (fechas relativas a hoy para que siempre haya funciones futuras)
INSERT INTO funciones (pelicula_id, sala_id, fecha_hora, idioma) VALUES
(1, 2, TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY, '19:30:00'), 'SUBTITULADA'),
(1, 3, TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY, '17:00:00'), 'ESPANOL'),
(1, 4, TIMESTAMP(CURRENT_DATE + INTERVAL 2 DAY, '21:00:00'), 'SUBTITULADA'),
(2, 1, TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY, '16:00:00'), 'ESPANOL'),
(2, 1, TIMESTAMP(CURRENT_DATE + INTERVAL 1 DAY, '18:30:00'), 'ESPANOL'),
(2, 6, TIMESTAMP(CURRENT_DATE + INTERVAL 2 DAY, '15:00:00'), 'ESPANOL');
