# Clon de Cinemex – Proyecto final POO

**Universidad Iberoamericana · Programación Orientada a Objetos · Otoño 2026**
Juan Pablo Flores · Ingeniería en Ciencia de Datos

Aplicación de escritorio en **Java 17 + JavaFX** con el patrón **MVC**, construida con **Maven**, conectada a **MySQL** (en Docker) y que obtiene su catálogo de películas de la **API de películas del grupo** (aprobada por el profesor), que a su vez toma los datos de **The Movie Database (TMDB)**.

Con la app puedes:

- **Explorar el catálogo de películas:** populares, en cartelera y próximamente, con búsqueda, filtro por género y paginación.
- **Ver el detalle de una película** y, como administrador, **importarla** a la cartelera de tu cine.
- **Consultar horarios** por ciudad, cine y día.
- **Comprar boletos** eligiendo asientos en un mapa, con varios tipos de boleto, y pagar con tarjeta o PayPal (simulado).
- **Ver tus compras y cancelarlas.**
- **Administrar** (CRUD) películas, cines, salas y funciones.

---

## Tecnologías

| Componente | Herramienta |
| --- | --- |
| Lenguaje | Java 17 o más reciente |
| Construcción | Maven (`pom.xml`) |
| Interfaz gráfica | JavaFX 21 (vistas FXML + CSS) |
| Base de datos | MySQL 8.4 en Docker (`docker-compose.yml`) |
| Catálogo | [API de películas del grupo](https://api-cinemex-akd3btgfhsgnf5d5.northcentralus-01.azurewebsites.net/api/peliculas/swagger) (datos de TMDB) con `java.net.http.HttpClient` y Gson. Alternativa: TMDB directo |

---

## Cómo correrlo (macOS, también funciona en Windows y Linux)

**1. Requisitos:**
- Java 17 o más reciente (`java -version`).
- Maven (`mvn -version`; en Mac: `brew install maven`).
- Docker Desktop abierto.

**2. Credenciales.** No se suben a GitHub: `.env` y `config.properties` están en `.gitignore`.

```bash
cp .env.example .env                              # contraseñas de MySQL para Docker
cp config.properties.example config.properties    # MySQL + llave de TMDB para la app
```

- En `.env`, cambia las contraseñas.
- En `config.properties`, pon la **misma** `db.password` que pusiste en `MYSQL_PASSWORD`.
- El catálogo usa por defecto la API del grupo (`peliculas.fuente=api`), que **no necesita llave**.
- Si algún día quieres usar TMDB directo, pon `peliculas.fuente=tmdb` y tu token en `tmdb.token`. El token se saca en themoviedb.org → Ajustes → API.

**3. Levantar MySQL** (crea la base con `db/schema.sql` y la llena con `db/data.sql`):

```bash
./scripts/levantar-mysql.sh            # la primera vez
./scripts/levantar-mysql.sh --reset    # para borrar todo y volver a cargar los datos de prueba
```

MySQL queda en `localhost:3307`. Se usa el 3307 para no chocar con un MySQL local.

**4. Abrir la aplicación:**

```bash
mvn javafx:run
```

Desde VS Code o IntelliJ también se puede correr la clase `cinemex.Lanzador`.

### Usuarios de prueba

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | `admin@cinemex.mx` | `admin123` |
| Cliente | `cliente@cinemex.mx` | `cliente123` |

También puedes crear una cuenta de cliente desde la pantalla de inicio de sesión.

### Recorrido rápido

1. En la **Cartelera** cambia entre Populares, En cartelera y Próximamente. Busca un título, filtra por género y pasa de página.
2. Entra como **admin**, abre una película y presiona **Importar a mi cine**.
3. En **Administración → Funciones**, prográmale una función en una sala.
4. Cierra sesión y entra como **cliente**. Ve a **Mi cine** o al detalle de la película y elige **Comprar boletos**. Luego elige tus asientos, los tipos de boleto y paga.
5. En **Mis compras** ves el historial y puedes cancelar.

---

## Arquitectura MVC

```
src/main/java/cinemex/
├── App.java                 crea los Servicios una sola vez y abre la ventana
├── Lanzador.java            para correr desde el IDE
├── base/                    Config (config.properties), ConexionBD, Seguridad (SHA-256), ExcepcionCine
├── modelo/                  M: los POJOs del diagrama de clases
│   ├── usuarios/            POJOUsuario (abstracta), POJOCliente, POJOAdministrador
│   ├── peliculas/           POJOPelicula, POJOGenero, Clasificacion, IFiltroPelicula + 3 filtros
│   ├── cines/               POJOCine, POJOSala (abstracta) + Tradicional/Platino/X4D, POJOAsiento
│   ├── funciones/           POJOFuncion, Idioma
│   └── compras/             POJOCompra, POJOBoleto, TipoBoleto, EstadoCompra, IMetodoPago, PagoTarjeta, PagoPayPal
├── servicio/                M: reglas de negocio + MySQL (ServicioUsuarios, ServicioPeliculas,
│                               ServicioCines, ServicioFunciones, ServicioCompras)
├── catalogo/                IClientePeliculas (contrato del catálogo), PaginaPeliculas, ExcepcionCatalogo
├── api/                     ClienteCinemexAPI (API del grupo, la que se usa) y sus DTOs
├── tmdb/                    ClienteTMDB (TMDB directo, alternativa) y sus DTOs
└── controlador/             C: un controlador por pantalla + Navegador, Contexto, Tareas
src/main/resources/cinemex/
├── vista/*.fxml             V: las pantallas
└── estilos.css
```

- **Modelo:** los POJO guardan los datos y las reglas de cada objeto. Por ejemplo, `POJOSala.calcularPrecio()` es polimórfico y `POJOFuncion.estaDisponible()` dice si un asiento está libre. Los Servicios aplican las reglas de negocio y hablan con MySQL usando `PreparedStatement`.
- **Vista:** archivos FXML con estilos CSS. No tienen lógica.
- **Controlador:** recibe los eventos de la vista, llama a los Servicios y muestra los resultados. Nunca usan SQL ni JSON directamente.

---

## Catálogo de películas (API del grupo)

Con autorización del profesor, el catálogo se obtiene de la API de películas de mis compañeros, que expone datos de TMDB.

- Base: `https://api-cinemex-akd3btgfhsgnf5d5.northcentralus-01.azurewebsites.net/api/peliculas`
- Documentación: [Swagger](https://api-cinemex-akd3btgfhsgnf5d5.northcentralus-01.azurewebsites.net/api/peliculas/swagger)

| Funcionalidad | Endpoint | Dónde se usa |
| --- | --- | --- |
| Películas populares | `GET /populares?page=n` | Cartelera → "Populares" (pantalla de inicio) |
| En cartelera | `GET /cartelera?page=n` | Cartelera → "En cartelera" |
| Próximos estrenos | `GET /proximos-estrenos?page=n` | Cartelera → "Próximamente" |
| Detalles de película | `GET /{id}` | Pantalla de detalle (sinopsis, duración, géneros, cartel, clasificación) |
| Buscar película | *(la API no lo tiene)* | `ClienteCinemexAPI.buscar` junta las películas de las tres listas y las filtra con `FiltroTitulo`, la clase del diseño |
| Géneros | *(la API no lo tiene)* | `ClienteCinemexAPI.generos` arma el catálogo con los géneros de las películas y lo guarda en la tabla `generos` |

**Cómo está hecho:**

- **Una interfaz, dos fuentes.** Las pantallas solo conocen la interfaz **`IClientePeliculas`**, que tiene dos implementaciones:
  - `ClienteCinemexAPI`: la API del grupo, la que se usa.
  - `ClienteTMDB`: TMDB directo, con `/3/movie/popular`, `now_playing`, `upcoming`, `search/movie`, `/movie/{id}` y `genre/movie/list`.

  Se elige con `peliculas.fuente` en `config.properties`, sin tocar la interfaz gráfica (polimorfismo).
- **Paginación:** los botones "Anterior / Siguiente" piden la página correspondiente.
- **No congela la interfaz:** todas las llamadas van en un `javafx.concurrent.Task` (clase `Tareas`).
- **Errores:** sin internet, API caída (error 500), película no encontrada y búsqueda sin resultados muestran un mensaje en pantalla.
- **El JSON no llega a las vistas:** el cliente convierte los DTO a `POJOPelicula` y `POJOGenero`.
- **Catálogo vs. mi cine:** la API es el catálogo y MySQL es la cartelera del cine. El administrador **importa** la película (`ServicioPeliculas.guardar`) antes de programarle funciones. `tmdb_id` es `UNIQUE`: volver a importar actualiza la película y no la duplica.

---

## Base de datos

- **`db/schema.sql`:** tablas `usuarios`, `generos`, `peliculas`, `pelicula_genero`, `cines`, `salas`, `asientos`, `funciones`, `compras` y `boletos`, con llaves foráneas.
- **`db/data.sql`:** datos de prueba. Incluye usuarios (admin y cliente), géneros, 3 cines, 6 salas con todos sus asientos, 2 películas y 6 funciones en los próximos días.

**Cómo se cuida la integridad:**

| Regla | Cómo se garantiza |
| --- | --- |
| Un asiento no se vende dos veces para la misma función (RN01) | Se revisa en Java y además con `UNIQUE (funcion_id, asiento_id, vigente)`. Al cancelar, `vigente` pasa a `NULL` y el asiento se puede volver a vender. |
| La compra es "todo o nada" | Se guarda en una transacción (`commit` / `rollback`). |
| Un correo, un usuario (RN09) | `UNIQUE (email)` |
| No se duplica una película de TMDB | `UNIQUE (tmdb_id)` |
| Borrar un cine o una sala | `ON DELETE CASCADE` hacia sus salas y asientos (composición) |
| No se borra lo que tiene funciones | `ON DELETE RESTRICT` en películas y salas con funciones; se muestra un mensaje claro |
| Contraseñas | SHA-256 con sal, nunca en texto plano |

---

## Reglas de negocio (de la Entrega 1)

| Regla | Dónde está en el código |
| --- | --- |
| RN01 asiento único por función | `POJOFuncion.ocupar`, `ServicioCompras.confirmarCompra` (vuelve a leer los asientos ocupados), llave única en `boletos` |
| RN02 función = película + sala + hora | Constructor de `POJOFuncion`, sin setters para esos datos |
| RN03 sin empalmes en una sala (+20 min de limpieza) | `ServicioFunciones.guardar` y `POJOFuncion.calcularHoraFin` |
| RN04 de 1 a 10 boletos, sin repetir asiento | `POJOCompra.agregarBoleto` y `tieneAsiento` |
| RN05 solo clientes con sesión compran | `Navegador.requiereSesion` y `ServicioCompras.iniciarCompra` |
| RN06 los asientos se ocupan solo si el pago es válido | `ServicioCompras.confirmarCompra` llama a `IMetodoPago.validar()` antes de guardar |
| RN07 precio según sala y tipo de boleto | `POJOSala.calcularPrecio` (polimorfismo) y `TipoBoleto.getFactor` |
| RN08 cancelar antes de la función libera asientos | `ServicioCompras.cancelar` |
| RN09 correo único | `ServicioUsuarios.guardar` y `UNIQUE (email)` |
| RN10 solo el admin administra | Menú "Administración" visible solo con `esAdministrador()` |

---

## Documentación y diagramas

- `docs/entrega1/`: PDF y diagramas UML de la Entrega 1 (análisis y diseño).
- `docs/entrega2/diagramas/`: **diagrama de clases actualizado** a lo que está programado. Incluye una vista general del modelo y un diagrama por paquete, más `clases.puml` editable.
- `docs/entrega2/gen/`: scripts que generan esos diagramas (`python3 gen_clases.py ../diagramas`).

---

## Cambios respecto al diseño

Al programar cambiaron algunas cosas del diseño de la Entrega 1. Todas están reflejadas en `docs/entrega2/diagramas/`.

| # | Qué cambió | Por qué |
| --- | --- | --- |
| 1 | Las clases `MenuX` y la interfaz `IMenu` (consola con `Scanner`) se reemplazaron por **vistas FXML + controladores** (`XControlador extends ControladorBase`), con `Navegador` y `Contexto`. `App` ahora extiende `Application`. | La entrega pide JavaFX con MVC. Como el diseño ya separaba POJO / Servicio / Menu, solo cambió la capa Menu; el modelo y los Servicios se mantuvieron. |
| 2 | Los paquetes se organizaron **por capa**: `modelo.*`, `servicio`, `controlador`, `tmdb`. Antes eran por módulo. | Para que la separación MVC se vea en la estructura. Dentro de `modelo` se conservan los módulos del diseño (usuarios, películas, cines, funciones, compras). |
| 3 | `POJOPelicula` ahora tiene `tmdbId`, `posterPath`, `fechaEstreno`, `calificacion` y una **lista de `POJOGenero`**. Se quitaron `genero: String` y `director`. | El catálogo viene de TMDB: una película puede tener varios géneros, y el director no viene en `/movie/{id}`. Se agregó la clase **`POJOGenero`** con las tablas `generos` y `pelicula_genero`. |
| 4 | `Clasificacion` agrega `SC` (sin clasificar). | TMDB no siempre trae la clasificación mexicana. El administrador la puede corregir. |
| 5 | Paquetes nuevos para el catálogo: **`catalogo`** (interfaz `IClientePeliculas`, `PaginaPeliculas`, `ExcepcionCatalogo`), **`api`** (`ClienteCinemexAPI` y sus DTO) y **`tmdb`** (`ClienteTMDB` y sus DTO). | Consumir la API sin que el JSON llegue a las vistas, soportar paginación y poder cambiar de fuente sin tocar la interfaz gráfica. |
| 6 | Los Servicios usan **MySQL** en lugar de listas en memoria. Se agregaron métodos para el CRUD completo: `actualizar`, `eliminar`, `eliminarSala`, `buscarPorTmdbId`, `sincronizarGeneros`, `listarCiudades`, `listarPorCineYDia`, `cargarOcupados`, `contarBoletos`, `registrarCliente`, `getClienteActual`. | Persistencia y operaciones CRUD desde la interfaz. |
| 7 | `ServicioPeliculas.guardar` funciona como **importar desde TMDB**: inserta, o actualiza si el `tmdb_id` ya existe. | Regla del enunciado: no guardar la misma película dos veces. |
| 8 | `ServicioCines.crearSala(...)` es la **fábrica** que decide qué subclase de sala crear. `agregarSala` guarda la sala con todos sus asientos. | Un solo lugar sabe qué subclase usar, tanto al leer de MySQL como en la pantalla de administración. |
| 9 | `POJOUsuario.password` pasó a `passwordHash` + `sal`, y se agregó la clase `Seguridad`. | RNF02: no guardar contraseñas en texto plano. |
| 10 | `POJOSala` guarda `cineId`, y `POJOFuncion` guarda también su `POJOCine`. | Para mapear la llave foránea y mostrar el cine de una función sin consultas extra. `POJOCine.tieneSala` se sigue usando para filtrar. |
| 11 | El descuento por tipo de boleto vive en el enum `TipoBoleto` (factor 1.0 o 0.7). Antes cada sala hacía un `switch`. | Evita repetir el mismo código en las tres subclases de sala. |
| 12 | `MAX_BOLETOS` se definió en `POJOCompra`, y `ServicioCompras` lo reutiliza. | El modelo no debe depender de la capa de servicio. |
| 13 | `IMetodoPago` agrega `getNombre()` y `getReferencia()`. | Para guardar en la base el método de pago y solo los últimos 4 dígitos de la tarjeta. |
| 14 | Los Servicios lanzan **`ExcepcionCine`** con un mensaje para el usuario cuando se rompe una regla (siguen regresando `boolean` si todo sale bien). | Que la interfaz pueda mostrar *por qué* falló (por ejemplo "Se empalma con…"), no solo `false`. |
| 15 | La compra `PENDIENTE` vive solo en memoria y se guarda en MySQL al pagar, dentro de una transacción. | RN06 y RNF04: no se aparta nada hasta pagar, y se guarda todo o nada. |
| 16 | En la pantalla de compra se eligen primero los asientos y después los tipos de boleto. | Así funciona cinemex.com, como se ve en el análisis. |
| 17 | El catálogo se obtiene de la **API de películas del grupo** en lugar de TMDB directo. Como esa API no tiene búsqueda ni lista de géneros, `ClienteCinemexAPI` los resuelve con los datos de las listas (búsqueda con `FiltroTitulo`). `ClienteTMDB` se conserva como alternativa. | Autorizado por el profesor. La interfaz `IClientePeliculas` permite cambiar de fuente sin modificar los controladores. |

---

## Atribución

<!-- Logo oficial de TMDB: descárgalo de https://www.themoviedb.org/about/logos-attribution
     y guárdalo como docs/img/tmdb-logo.png y src/main/resources/cinemex/img/tmdb-logo.png -->
![TMDB](docs/img/tmdb-logo.png)

**This product uses the TMDB API but is not endorsed or certified by TMDB.**

Catálogo, pósters y sinopsis: [The Movie Database (TMDB)](https://www.themoviedb.org/), a través de la API de películas del grupo. Cinemex es una marca de su respectivo dueño; este es un proyecto escolar sin fines comerciales.
