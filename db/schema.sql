-- =====================================================================
--  Clon de Cinemex - esquema de la base de datos (MySQL 8)
--  Cada tabla corresponde a un POJO del diagrama de clases.
-- =====================================================================
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS cinemex CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cinemex;

-- POJOUsuario / POJOCliente / POJOAdministrador (herencia en una sola tabla, columna "rol")
CREATE TABLE usuarios (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash CHAR(64)     NOT NULL,          -- SHA-256 en hexadecimal (RNF02)
    sal           VARCHAR(32)  NOT NULL,
    rol           ENUM('CLIENTE','ADMINISTRADOR') NOT NULL DEFAULT 'CLIENTE',
    telefono      VARCHAR(20)  NULL,              -- solo clientes
    num_empleado  VARCHAR(20)  NULL,              -- solo administradores
    CONSTRAINT uq_usuarios_email UNIQUE (email)   -- RN09: un correo, un usuario
);

-- POJOGenero: catálogo de géneros que viene de /3/genre/movie/list
CREATE TABLE generos (
    id     INT PRIMARY KEY,                        -- mismo id que TMDB
    nombre VARCHAR(60) NOT NULL
);

-- POJOPelicula: solo las películas importadas de TMDB a "mi cine"
CREATE TABLE peliculas (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    tmdb_id        INT          NOT NULL,
    titulo         VARCHAR(200) NOT NULL,
    sinopsis       TEXT         NULL,
    duracion_min   INT          NOT NULL DEFAULT 0,
    clasificacion  ENUM('AA','A','B','B15','C','D','SC') NOT NULL DEFAULT 'SC',
    poster_path    VARCHAR(200) NULL,
    fecha_estreno  DATE         NULL,
    en_cartelera   BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_peliculas_tmdb UNIQUE (tmdb_id)  -- no guardar la misma película dos veces
);

-- Relación muchos a muchos Película <-> Género
CREATE TABLE pelicula_genero (
    pelicula_id INT NOT NULL,
    genero_id   INT NOT NULL,
    PRIMARY KEY (pelicula_id, genero_id),
    CONSTRAINT fk_pg_pelicula FOREIGN KEY (pelicula_id) REFERENCES peliculas(id) ON DELETE CASCADE,
    CONSTRAINT fk_pg_genero   FOREIGN KEY (genero_id)   REFERENCES generos(id)
);

-- POJOCine
CREATE TABLE cines (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    ciudad    VARCHAR(80)  NOT NULL,
    direccion VARCHAR(200) NOT NULL
);

-- POJOSala (+ subclases Tradicional / Platino / X4D en la columna "tipo")
-- Composición: si se borra el cine se borran sus salas (ON DELETE CASCADE)
CREATE TABLE salas (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    cine_id       INT NOT NULL,
    numero        INT NOT NULL,
    tipo          ENUM('TRADICIONAL','PLATINO','X4D') NOT NULL,
    filas         INT NOT NULL,
    columnas      INT NOT NULL,
    cargo_efectos DECIMAL(8,2) NOT NULL DEFAULT 0,   -- solo X4D
    CONSTRAINT uq_salas_numero UNIQUE (cine_id, numero),
    CONSTRAINT ck_salas_tamano CHECK (filas BETWEEN 1 AND 26 AND columnas BETWEEN 1 AND 30),
    CONSTRAINT fk_salas_cine FOREIGN KEY (cine_id) REFERENCES cines(id) ON DELETE CASCADE
);

-- POJOAsiento: la silla física. Composición con la sala.
CREATE TABLE asientos (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    sala_id INT     NOT NULL,
    fila    CHAR(1) NOT NULL,
    numero  INT     NOT NULL,
    CONSTRAINT uq_asientos UNIQUE (sala_id, fila, numero),
    CONSTRAINT fk_asientos_sala FOREIGN KEY (sala_id) REFERENCES salas(id) ON DELETE CASCADE
);

-- POJOFuncion: película + sala + fecha y hora (RN02)
-- RESTRICT: no se puede borrar una película o sala que tiene funciones
CREATE TABLE funciones (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    pelicula_id INT      NOT NULL,
    sala_id     INT      NOT NULL,
    fecha_hora  DATETIME NOT NULL,
    idioma      ENUM('ESPANOL','SUBTITULADA') NOT NULL,
    CONSTRAINT fk_funciones_pelicula FOREIGN KEY (pelicula_id) REFERENCES peliculas(id) ON DELETE RESTRICT,
    CONSTRAINT fk_funciones_sala     FOREIGN KEY (sala_id)     REFERENCES salas(id)     ON DELETE RESTRICT
);

-- POJOCompra
CREATE TABLE compras (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id     INT NOT NULL,
    funcion_id     INT NOT NULL,
    metodo_pago    VARCHAR(20)  NULL,           -- 'Tarjeta' o 'PayPal'
    referencia     VARCHAR(100) NULL,           -- últimos 4 dígitos o correo de PayPal (RNF02)
    estado         ENUM('PENDIENTE','PAGADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total          DECIMAL(10,2) NOT NULL DEFAULT 0,
    comision       DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_compras_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_compras_funcion FOREIGN KEY (funcion_id) REFERENCES funciones(id)
);

-- POJOBoleto (composición con la compra)
-- RN01: un asiento no se vende dos veces para la misma función.
--   "vigente" vale 1 mientras el boleto cuenta y NULL cuando la compra se cancela.
--   Como MySQL permite varios NULL en un UNIQUE, un asiento cancelado se puede volver a vender.
CREATE TABLE boletos (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    compra_id  INT NOT NULL,
    funcion_id INT NOT NULL,
    asiento_id INT NOT NULL,
    tipo       ENUM('ADULTO','NINO','ADULTO_MAYOR') NOT NULL,
    precio     DECIMAL(8,2) NOT NULL,
    vigente    TINYINT NULL DEFAULT 1,
    CONSTRAINT uq_boletos_asiento UNIQUE (funcion_id, asiento_id, vigente),
    CONSTRAINT fk_boletos_compra  FOREIGN KEY (compra_id)  REFERENCES compras(id) ON DELETE CASCADE,
    CONSTRAINT fk_boletos_funcion FOREIGN KEY (funcion_id) REFERENCES funciones(id),
    CONSTRAINT fk_boletos_asiento FOREIGN KEY (asiento_id) REFERENCES asientos(id)
);
