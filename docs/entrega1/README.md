# Clon de Cinemex – Proyecto final POO (Ibero)

**Entrega 1: Análisis y diseño UML.** En esta entrega no hay código; solo el diseño que se implementará en Java en la Entrega 2.

## Contenido

| Carpeta / archivo | Qué es |
| --- | --- |
| `Entrega1_Cinemex.pdf` | Documento de la entrega (exportarlo desde el doc y ponerlo aquí) |
| `diagramas/casos_de_uso.png` | Diagrama de casos de uso |
| `diagramas/actividad_consultar_pelicula.png` | Actividades 1: consultar película y encontrar función |
| `diagramas/actividad_comprar_boletos.png` | Actividades 2: comprar boletos |
| `diagramas/clases_dominio.png` | Diagrama de clases, vista general |
| `diagramas/clases_<modulo>.png` | Diagrama de clases por módulo (base, usuarios, peliculas, cines, funciones, compras) |
| `diagramas/secuencia_comprar_boletos.png` | Diagrama de secuencia de la compra |
| `diagramas/*.puml` | Fuente PlantUML editable (draw.io: Organizar → Insertar → Avanzado → PlantUML) |
| `diagramas/*.dot` | Fuente Graphviz con la que se generaron las imágenes |
| `gen/` | Scripts de Python que generan todos los diagramas desde `modelo.py` |

## Arquitectura (igual que el proyecto Airbnb)

```
src/
  App.java                         ← main, crea servicios y menús
  cinemex/base/        IMenu, ConexionBD
  cinemex/usuarios/    POJOUsuario, POJOCliente, POJOAdministrador, ServicioUsuarios, MenuUsuarios
  cinemex/peliculas/   POJOPelicula, Clasificacion, IFiltroPelicula, Filtro*, ServicioPeliculas, MenuPeliculas
  cinemex/cines/       POJOCine, POJOSala, POJOSala{Tradicional,Platino,X4D}, POJOAsiento, ServicioCines, MenuCines
  cinemex/funciones/   POJOFuncion, Idioma, ServicioFunciones, MenuFunciones
  cinemex/compras/     POJOCompra, POJOBoleto, TipoBoleto, EstadoCompra, IMetodoPago, PagoTarjeta, PagoPayPal,
                       ServicioCompras, MenuCompras
```

- **POJO**: datos y reglas propias del objeto.
- **Servicio**: lista de objetos + reglas de negocio (`guardar`, `buscarPorId`, `listar`, …).
- **Menu**: habla con el usuario (`IMenu`). En la versión con interfaz gráfica solo se reemplaza esta capa.

## Regenerar los diagramas

```bash
cd gen
python3 gen_clases.py ../diagramas
python3 gen_otros.py ../diagramas
python3 gen_secuencia.py ../diagramas
```

Requiere Graphviz (`brew install graphviz` en macOS) y matplotlib (`pip3 install matplotlib`).
