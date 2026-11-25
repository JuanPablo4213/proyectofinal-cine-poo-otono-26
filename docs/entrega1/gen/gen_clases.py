import html, subprocess, sys
from modelo import CLASES, RELACIONES, PAQUETES

OUT = sys.argv[1]
COLORES = {"base": "#ECEFF1", "usuarios": "#E3F2FD", "peliculas": "#FFF3E0", "cines": "#E8F5E9",
           "funciones": "#F3E5F5", "compras": "#FFEBEE"}

def esc(s):
    return html.escape(s)

def etiqueta(nombre, est, attrs, mets, color):
    cab = ""
    if est:
        cab = f'<FONT POINT-SIZE="9">«{est}»</FONT><BR/>'
    titulo = f'<I>{esc(nombre)}</I>' if est in ("abstract", "interface") else esc(nombre)
    filas = [f'<TR><TD BGCOLOR="{color}">{cab}<B>{titulo}</B></TD></TR>']
    a = "<BR ALIGN=\"LEFT\"/>".join(esc(x) for x in attrs)
    m = "<BR ALIGN=\"LEFT\"/>".join(esc(x) for x in mets)
    if est != "interface":
        filas.append(f'<TR><TD ALIGN="LEFT" BALIGN="LEFT">{a}{"<BR ALIGN=\"LEFT\"/>" if a else " "}</TD></TR>')
    if est != "enumeration":
        filas.append(f'<TR><TD ALIGN="LEFT" BALIGN="LEFT">{m}{"<BR ALIGN=\"LEFT\"/>" if m else " "}</TD></TR>')
    return '<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">' + "".join(filas) + "</TABLE>>"

def estilo(tipo):
    return {
        "hereda": 'arrowhead=empty, style=solid',
        "implementa": 'arrowhead=empty, style=dashed',
        "compone": 'arrowtail=diamond, dir=back, arrowhead=vee',
        "agrega": 'arrowtail=odiamond, dir=back, arrowhead=vee',
        "asocia": 'arrowhead=vee',
        "usa": 'arrowhead=vee, style=dashed, color="#777777", fontcolor="#777777"',
    }[tipo]

def dot(clases_filtro=None, sin_menus=False, nombre="clases"):
    nombres = set()
    out = ['digraph G {', 'graph [rankdir=BT, splines=true, nodesep=0.35, ranksep=0.6, fontname="Helvetica", '
           'label="Diagrama de clases – Clon de Cinemex", labelloc=t, fontsize=20, pad=0.3, newrank=true];',
           'node [shape=plain, fontname="Helvetica", fontsize=10];',
           'edge [fontname="Helvetica", fontsize=9];']
    for paq in PAQUETES:
        bloque = []
        for (p, n, est, at, me) in CLASES:
            if p != paq:
                continue
            if clases_filtro and n not in clases_filtro:
                continue
            if sin_menus and (n.startswith("Menu") or n in ("App", "ConexionBD", "IMenu")):
                continue
            nombres.add(n)
            bloque.append(f'"{n}" [label={etiqueta(n, est, at, me, COLORES[p])}];')
        if bloque:
            out.append(f'subgraph cluster_{paq} {{ label="{PAQUETES[paq]}"; style="rounded,dashed"; '
                       f'color="#90A4AE"; fontsize=13; fontcolor="#455A64";')
            out += bloque
            out.append('}')
    for (o, d, t, lo, ld, tx) in RELACIONES:
        if o in nombres and d in nombres:
            attrs = [estilo(t)]
            if tx: attrs.append(f'label="{tx}"')
            if t == "compone" or t == "agrega":
                if lo: attrs.append(f'taillabel="{lo}"')
                if ld: attrs.append(f'headlabel="{ld}"')
            else:
                if lo: attrs.append(f'taillabel="{lo}"')
                if ld: attrs.append(f'headlabel="{ld}"')
            if t in ("usa",):
                attrs.append("constraint=false")
            out.append(f'"{o}" -> "{d}" [{", ".join(attrs)}];')
    out.append('}')
    src = "\n".join(out)
    open(f"{OUT}/{nombre}.dot", "w").write(src)
    subprocess.run(["dot", "-Tpng", "-Gdpi=130", f"{OUT}/{nombre}.dot", "-o", f"{OUT}/{nombre}.png"], check=True)
    subprocess.run(["dot", "-Tsvg", f"{OUT}/{nombre}.dot", "-o", f"{OUT}/{nombre}.svg"], check=True)

def puml():
    tipos = {"hereda": "<|--", "implementa": "<|..", "compone": "*--", "agrega": "o--", "asocia": "-->", "usa": "..>"}
    L = ["@startuml clases_cinemex", "title Diagrama de clases - Clon de Cinemex", "skinparam classAttributeIconSize 0",
         "left to right direction", ""]
    for paq, full in PAQUETES.items():
        L.append(f'package {full} {{')
        for (p, n, est, at, me) in CLASES:
            if p != paq: continue
            kw = {"interface": "interface", "abstract": "abstract class", "enumeration": "enum"}.get(est, "class")
            L.append(f"  {kw} {n} {{")
            for a in at: L.append(f"    {a}")
            for m in me: L.append(f"    {m}")
            L.append("  }")
        L.append("}")
    L.append("")
    for (o, d, t, lo, ld, tx) in RELACIONES:
        if t in ("hereda", "implementa"):
            L.append(f"{d} {tipos[t]} {o}")
        else:
            a = f'"{lo}" ' if lo else ""
            b = f' "{ld}"' if ld else ""
            lab = f" : {tx}" if tx else ""
            L.append(f"{o} {a}{tipos[t]}{b} {d}{lab}")
    L.append("@enduml")
    open(f"{OUT}/clases.puml", "w").write("\n".join(L) + "\n")

dot(nombre="clases_completo")
dot(sin_menus=True, nombre="clases_dominio")
puml()
print("ok")

def modulo(paq):
    propios = [c for c in CLASES if c[0] == paq]
    nom_propios = {c[1] for c in propios}
    ext = set()
    for (o, d, t, *_r) in RELACIONES:
        if o in nom_propios and d not in nom_propios: ext.add(d)
        if d in nom_propios and o not in nom_propios and not o.startswith("Menu") and o != "App": ext.add(o)
    out = ['digraph G {', f'graph [rankdir=BT, nodesep=0.4, ranksep=0.7, fontname="Helvetica", '
           f'label="Diagrama de clases – módulo {PAQUETES[paq]}", labelloc=t, fontsize=18, pad=0.3];',
           'node [shape=plain, fontname="Helvetica", fontsize=10];', 'edge [fontname="Helvetica", fontsize=9];']
    for (p, n, est, at, me) in propios:
        out.append(f'"{n}" [label={etiqueta(n, est, at, me, COLORES[p])}];')
    for (p, n, est, at, me) in CLASES:
        if n in ext:
            cab = f'<FONT POINT-SIZE="8">«{est}» </FONT>' if est else ""
            out.append(f'"{n}" [label=<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="5"><TR><TD BGCOLOR="{COLORES[p]}" STYLE="dashed">{cab}{n}<BR/><FONT POINT-SIZE="8" COLOR="#666666">({PAQUETES[p]})</FONT></TD></TR></TABLE>>];')
    todos = nom_propios | ext
    for (o, d, t, lo, ld, tx) in RELACIONES:
        if o in todos and d in todos and (o in nom_propios or d in nom_propios):
            attrs = [estilo(t)]
            if tx: attrs.append(f'label="{tx}"')
            if lo: attrs.append(f'taillabel="{lo}"')
            if ld: attrs.append(f'headlabel="{ld}"')
            out.append(f'"{o}" -> "{d}" [{", ".join(attrs)}];')
    out.append("}")
    f = f"{OUT}/clases_{paq}"
    open(f + ".dot", "w").write("\n".join(out))
    subprocess.run(["dot", "-Tpng", "-Gdpi=140", f + ".dot", "-o", f + ".png"], check=True)

for p in PAQUETES:
    modulo(p)
