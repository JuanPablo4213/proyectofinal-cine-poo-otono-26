import subprocess, sys
OUT = sys.argv[1]

def render(nombre, src, dpi=140):
    open(f"{OUT}/{nombre}.dot", "w").write(src)
    subprocess.run(["dot", "-Tpng", f"-Gdpi={dpi}", f"{OUT}/{nombre}.dot", "-o", f"{OUT}/{nombre}.png"], check=True)

# ======================= CASOS DE USO =======================
CU = {
    "Visitante": ["CU01 Consultar cartelera", "CU02 Buscar película", "CU03 Ver detalle de película",
                  "CU04 Seleccionar cine", "CU05 Consultar horarios de funciones",
                  "CU06 Ver asientos disponibles", "CU07 Registrarse", "CU08 Iniciar sesión"],
    "Cliente registrado": ["CU09 Comprar boletos", "CU10 Seleccionar asientos", "CU11 Pagar compra",
                           "CU12 Consultar mis compras", "CU13 Cancelar compra", "CU14 Cerrar sesión"],
    "Administrador": ["CU15 Gestionar películas", "CU16 Gestionar cines y salas", "CU17 Programar funciones"],
}
def usecases():
    L = ['digraph CU {', 'graph [rankdir=LR, nodesep=0.25, ranksep=1.4, fontname="Helvetica", '
         'label="Diagrama de casos de uso – Clon de Cinemex", labelloc=t, fontsize=20, pad=0.3];',
         'node [fontname="Helvetica", fontsize=11];', 'edge [arrowhead=none, fontname="Helvetica", fontsize=10];']
    actor = lambda n: f'"{n}" [shape=none, image="", label=<<TABLE BORDER="0"><TR><TD><FONT POINT-SIZE="34">🯅</FONT></TD></TR></TABLE>>];'
    for a in CU:
        L.append(f'"{a}" [shape=box, style="rounded,filled", fillcolor="#FFF8E1", label="«actor»\\n{a}", width=1.6];')
    L.append('subgraph cluster_sis { label="Sistema Cinemex (clon)"; style=rounded; color="#455A64"; fontsize=14;')
    for a, cs in CU.items():
        for c in cs:
            L.append(f'"{c}" [shape=ellipse, style=filled, fillcolor="#E3F2FD", label="{c[5:]}\\n({c[:4]})"];')
    L.append('}')
    # generalización: Cliente hereda de Visitante
    L.append('"Cliente registrado" -> "Visitante" [arrowhead=empty, constraint=false, label=" hereda"];')
    for a, cs in CU.items():
        for c in cs:
            L.append(f'"{a}" -> "{c}";')
    L.append('"Administrador" -> "CU08 Iniciar sesión";')
    L.append('"Administrador" -> "CU14 Cerrar sesión";')
    L.append('{rank=same; "Visitante"; "Cliente registrado"; "Administrador";}')
    L.append('}')
    render("casos_de_uso", "\n".join(L))

# ======================= ACTIVIDADES =======================
def actividad(nombre, titulo, nodos, aristas):
    L = ['digraph A {', f'graph [rankdir=TB, nodesep=0.4, ranksep=0.35, fontname="Helvetica", label="{titulo}", '
         'labelloc=t, fontsize=18, pad=0.3];', 'node [fontname="Helvetica", fontsize=11];',
         'edge [fontname="Helvetica", fontsize=10, arrowsize=0.8];']
    for (i, tipo, txt) in nodos:
        if tipo == "ini": L.append(f'{i} [shape=circle, style=filled, fillcolor=black, label="", width=0.3];')
        elif tipo == "fin": L.append(f'{i} [shape=doublecircle, style=filled, fillcolor=black, label="", width=0.22];')
        elif tipo == "dec": L.append(f'{i} [shape=diamond, style=filled, fillcolor="#FFF3E0", label="{txt}", fontsize=10];')
        elif tipo == "sis": L.append(f'{i} [shape=box, style="rounded,filled", fillcolor="#E8F5E9", label="{txt}"];')
        else: L.append(f'{i} [shape=box, style="rounded,filled", fillcolor="#E3F2FD", label="{txt}"];')
    for a in aristas:
        o, d = a[0], a[1]
        lab = f' [label="{a[2]}"]' if len(a) > 2 else ""
        L.append(f"{o} -> {d}{lab};")
    L.append('leyenda [shape=plain, label=<<TABLE BORDER="0" CELLSPACING="4"><TR><TD BGCOLOR="#E3F2FD" BORDER="1">Acción del usuario</TD><TD BGCOLOR="#E8F5E9" BORDER="1">Acción del sistema</TD><TD BGCOLOR="#FFF3E0" BORDER="1">Decisión</TD></TR></TABLE>>];')
    L.append('}')
    render(nombre, "\n".join(L))

def actividad1():
    n = [("i", "ini", ""),
         ("a1", "usr", "Abrir la cartelera"),
         ("a2", "sis", "Mostrar películas en cartelera\\n(ServicioPeliculas.listarCartelera)"),
         ("d1", "dec", "¿Buscar\\npor filtro?"),
         ("a3", "usr", "Escribir título / género /\\nclasificación"),
         ("a4", "sis", "Aplicar filtros\\n(ServicioPeliculas.buscar)"),
         ("d2", "dec", "¿Hay\\nresultados?"),
         ("a5", "sis", "Mostrar \\\"No se encontraron películas\\\""),
         ("a6", "usr", "Seleccionar una película"),
         ("a7", "sis", "Mostrar detalle: sinopsis, duración,\\nclasificación, género"),
         ("a8", "usr", "Seleccionar ciudad y cine"),
         ("a9", "usr", "Seleccionar día"),
         ("a10", "sis", "Buscar funciones\\n(ServicioFunciones.listarPorCineYPelicula)"),
         ("d3", "dec", "¿Hay\\nfunciones?"),
         ("a11", "sis", "Avisar que no hay funciones\\nese día en ese cine"),
         ("d4", "dec", "¿Probar otro\\ncine/día?"),
         ("a12", "sis", "Mostrar horarios con sala\\ne idioma"),
         ("a13", "usr", "Seleccionar una función"),
         ("f", "fin", ""),
         ("f2", "fin", "")]
    e = [("i", "a1"), ("a1", "a2"), ("a2", "d1"), ("d1", "a3", "sí"), ("d1", "a6", "no"), ("a3", "a4"),
         ("a4", "d2"), ("d2", "a5", "no"), ("a5", "a3"), ("d2", "a6", "sí"), ("a6", "a7"), ("a7", "a8"),
         ("a8", "a9"), ("a9", "a10"), ("a10", "d3"), ("d3", "a11", "no"), ("a11", "d4"), ("d4", "a8", "sí"),
         ("d4", "f2", "no"), ("d3", "a12", "sí"), ("a12", "a13"), ("a13", "f")]
    actividad("actividad_consultar_pelicula", "Diagrama de actividades 1 – Consultar una película y encontrar una función", n, e)

def actividad2():
    n = [("i", "ini", ""),
         ("a1", "usr", "Seleccionar una función"),
         ("d1", "dec", "¿Sesión\\niniciada?"),
         ("a2", "usr", "Iniciar sesión o registrarse"),
         ("d2", "dec", "¿Datos\\ncorrectos?"),
         ("a3", "sis", "Crear compra PENDIENTE\\n(ServicioCompras.iniciarCompra)"),
         ("a4", "sis", "Mostrar mapa de asientos\\n(POJOFuncion.getAsientosDisponibles)"),
         ("a5", "usr", "Elegir asiento y tipo de boleto\\n(Adulto / Niño / Adulto mayor)"),
         ("d3", "dec", "¿Asiento\\ndisponible?"),
         ("a6", "sis", "Mostrar \\\"Asiento ocupado\\\""),
         ("a7", "sis", "Calcular precio (POJOSala.calcularPrecio)\\ny agregar boleto a la compra"),
         ("d4", "dec", "¿Agregar otro\\nboleto? (máx. 10)"),
         ("a8", "sis", "Mostrar resumen y total"),
         ("a9", "usr", "Elegir método de pago\\ny capturar datos"),
         ("d5", "dec", "¿Pago\\nválido?"),
         ("a10", "sis", "Mostrar error de pago"),
         ("d6", "dec", "¿Reintentar?"),
         ("d7", "dec", "¿Los asientos\\nsiguen libres?"),
         ("a11", "sis", "Avisar: otro cliente compró\\nun asiento; volver a elegir"),
         ("a12", "sis", "Ocupar asientos en la función\\n(POJOFuncion.ocupar)"),
         ("a13", "sis", "Cambiar compra a PAGADA"),
         ("a14", "sis", "Mostrar confirmación y boletos"),
         ("a15", "sis", "Compra se queda sin pagar\\n(no se ocupa ningún asiento)"),
         ("f", "fin", ""), ("f2", "fin", "")]
    e = [("i", "a1"), ("a1", "d1"), ("d1", "a2", "no"), ("a2", "d2"), ("d2", "a2", "no"), ("d2", "a3", "sí"),
         ("d1", "a3", "sí"), ("a3", "a4"), ("a4", "a5"), ("a5", "d3"), ("d3", "a6", "no"), ("a6", "a5"),
         ("d3", "a7", "sí"), ("a7", "d4"), ("d4", "a5", "sí"), ("d4", "a8", "no"), ("a8", "a9"), ("a9", "d5"),
         ("d5", "a10", "no"), ("a10", "d6"), ("d6", "a9", "sí"), ("d6", "a15", "no"), ("a15", "f2"),
         ("d5", "d7", "sí"), ("d7", "a11", "no"), ("a11", "a4"), ("d7", "a12", "sí"), ("a12", "a13"),
         ("a13", "a14"), ("a14", "f")]
    actividad("actividad_comprar_boletos", "Diagrama de actividades 2 – Comprar boletos", n, e)

usecases(); actividad1(); actividad2()
print("ok")
