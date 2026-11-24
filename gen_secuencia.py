import sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle

OUT = sys.argv[1]

P = [("cli", "Cliente", True), ("menu", ":MenuCompras", False), ("su", ":ServicioUsuarios", False),
     ("sf", ":ServicioFunciones", False), ("sc", ":ServicioCompras", False), ("f", "f:POJOFuncion", False),
     ("s", "s:POJOSala", False), ("c", "c:POJOCompra", False), ("m", "m:PagoTarjeta\n«IMetodoPago»", False)]
X = {k: i * 2.3 for i, (k, _, _) in enumerate(P)}

# (tipo, origen, destino, texto)  tipo: call | ret | self | create | loop_ini | loop_fin | nota
M = [
    ("call", "cli", "menu", "1: elige \"Comprar boletos\" (idFuncion)"),
    ("call", "menu", "su", "2: getUsuarioActual()"),
    ("ret", "su", "menu", "cliente: POJOCliente"),
    ("call", "menu", "sf", "3: buscarPorId(idFuncion)"),
    ("ret", "sf", "menu", "f: POJOFuncion"),
    ("call", "menu", "sc", "4: iniciarCompra(cliente, f)"),
    ("create", "sc", "c", "4.1: «create» POJOCompra(id, cliente, f)"),
    ("ret", "sc", "menu", "c (estado PENDIENTE)"),
    ("call", "menu", "f", "5: getAsientosDisponibles()"),
    ("ret", "f", "menu", "List<POJOAsiento>"),
    ("ret", "menu", "cli", "6: muestra mapa de asientos"),
    ("loop_ini", None, None, "loop  [por cada boleto, máx. 10]"),
    ("call", "cli", "menu", "7: asiento \"F7\", tipo ADULTO"),
    ("call", "menu", "s", "8: f.getSala().buscarAsiento('F', 7)"),
    ("ret", "s", "menu", "a: POJOAsiento"),
    ("call", "menu", "sc", "9: agregarBoleto(c, a, ADULTO)"),
    ("call", "sc", "f", "9.1: estaDisponible(a)"),
    ("ret", "f", "sc", "true"),
    ("call", "sc", "s", "9.2: calcularPrecio(ADULTO)"),
    ("ret", "s", "sc", "precio"),
    ("call", "sc", "c", "9.3: agregarBoleto(new POJOBoleto(...))"),
    ("ret", "c", "sc", "true"),
    ("ret", "sc", "menu", "true"),
    ("loop_fin", None, None, ""),
    ("call", "cli", "menu", "10: datos de la tarjeta"),
    ("create", "menu", "m", "10.1: «create» PagoTarjeta(numero, cvv)"),
    ("call", "menu", "sc", "11: confirmarCompra(c, m)"),
    ("call", "sc", "m", "11.1: validar()"),
    ("ret", "m", "sc", "true"),
    ("loop_ini", None, None, "loop  [por cada boleto de c]"),
    ("call", "sc", "f", "11.2: estaDisponible(asiento)"),
    ("ret", "f", "sc", "true"),
    ("loop_fin", None, None, ""),
    ("call", "sc", "c", "11.3: calcularTotal()"),
    ("ret", "c", "sc", "total"),
    ("call", "sc", "m", "11.4: calcularComision(total)"),
    ("ret", "m", "sc", "comision"),
    ("loop_ini", None, None, "loop  [por cada boleto de c]"),
    ("call", "sc", "f", "11.5: ocupar(asiento)"),
    ("loop_fin", None, None, ""),
    ("call", "sc", "c", "11.6: setMetodo(m); setEstado(PAGADA)"),
    ("ret", "sc", "menu", "true"),
    ("ret", "menu", "cli", "12: muestra confirmación y boletos"),
]

DY = 0.55
alto = (len(M) + 3) * DY + 1.5
fig, ax = plt.subplots(figsize=(X["m"] + 2.5, alto))
ax.set_xlim(-1.3, X["m"] + 1.3)
ax.set_ylim(-alto + 0.3, 1.6)
ax.axis("off")
ax.set_title("Diagrama de secuencia – Comprar boletos (escenario: 1 boleto adulto, pago con tarjeta)", fontsize=15, pad=12)

y_fin = -(len(M) + 1) * DY
for k, nom, actor in P:
    x = X[k]
    if actor:
        ax.add_patch(plt.Circle((x, 1.25), 0.13, fill=False, lw=1.5))
        ax.plot([x, x], [1.12, 0.75], "k", lw=1.5); ax.plot([x - 0.2, x + 0.2], [1.0, 1.0], "k", lw=1.5)
        ax.plot([x, x - 0.18], [0.75, 0.5], "k", lw=1.5); ax.plot([x, x + 0.18], [0.75, 0.5], "k", lw=1.5)
        ax.text(x, 0.3, nom, ha="center", va="center", fontsize=10, fontweight="bold")
    else:
        ax.add_patch(FancyBboxPatch((x - 1.0, 0.1), 2.0, 0.75, boxstyle="round,pad=0.02",
                                    fc="#E3F2FD", ec="#37474F"))
        ax.text(x, 0.48, nom, ha="center", va="center", fontsize=9, fontweight="bold")
    ax.plot([x, x], [0.1, y_fin], color="#90A4AE", ls="--", lw=1, zorder=0)

y = -0.5
pila = []
for tipo, o, d, txt in M:
    y -= DY
    if tipo == "loop_ini":
        pila.append((y, txt)); y -= 0.25; continue
    if tipo == "loop_fin":
        y0, t = pila.pop()
        x0, x1 = X["cli"] - 0.6 + 0.4 * len(pila), X["m"] + 1.1 - 0.15 * len(pila)
        x0 = X["cli"] - 0.6
        ax.add_patch(Rectangle((x0, y), x1 - x0, y0 - y + 0.15, fill=False, ec="#6A1B9A", lw=1.2))
        ax.text(x0 + 0.08, y0 + 0.05, t, fontsize=9, color="#6A1B9A", va="top", fontweight="bold",
                bbox=dict(fc="white", ec="#6A1B9A", lw=0.8))
        continue
    xo, xd = X[o], X[d]
    if tipo == "call":
        ax.annotate("", xy=(xd, y), xytext=(xo, y), arrowprops=dict(arrowstyle="-|>", color="black", lw=1.1))
    elif tipo == "ret":
        ax.annotate("", xy=(xd, y), xytext=(xo, y),
                    arrowprops=dict(arrowstyle="->", color="#455A64", lw=1, ls="--"))
    elif tipo == "create":
        ax.annotate("", xy=(xd, y), xytext=(xo, y),
                    arrowprops=dict(arrowstyle="-|>", color="#2E7D32", lw=1.1, ls="--"))
    xm = min(xo, xd) + 0.1 if True else (xo + xd) / 2
    ax.text(xm, y + 0.07, txt, fontsize=8.5, va="bottom",
            color={"call": "black", "ret": "#455A64", "create": "#2E7D32"}[tipo],
            style="italic" if tipo == "ret" else "normal")

plt.savefig(f"{OUT}/secuencia_comprar_boletos.png", dpi=120, bbox_inches="tight")

# --- también la fuente PlantUML para que la puedan editar ---
L = ["@startuml secuencia_comprar_boletos", "title Diagrama de secuencia - Comprar boletos", "actor Cliente as cli"]
for k, nom, actor in P[1:]:
    L.append(f'participant "{nom.replace(chr(10), "\\n")}" as {k}')
for tipo, o, d, txt in M:
    if tipo == "loop_ini": L.append("loop " + txt.split("[")[1].rstrip("]"))
    elif tipo == "loop_fin": L.append("end")
    elif tipo == "create": L.append(f"{o} -->> {d} ** : {txt}".replace(" ** ", " ").replace("-->>", "-->"))
    elif tipo == "ret": L.append(f"{o} --> {d} : {txt}")
    else: L.append(f"{o} -> {d} : {txt}")
L.append("@enduml")
open(f"{OUT}/secuencia_comprar_boletos.puml", "w").write("\n".join(L) + "\n")
print("ok")
