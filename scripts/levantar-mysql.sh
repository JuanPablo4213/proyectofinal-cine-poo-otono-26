#!/usr/bin/env bash
# Levanta MySQL en Docker con el esquema y los datos de prueba.
# Uso: ./scripts/levantar-mysql.sh          (levanta)
#      ./scripts/levantar-mysql.sh --reset  (borra todo y vuelve a cargar schema.sql + data.sql)
set -e
cd "$(dirname "$0")/.."

if [ ! -f .env ]; then
  echo "Falta el archivo .env. Copia .env.example a .env y pon tus contraseñas:"
  echo "  cp .env.example .env"
  exit 1
fi

if [ "$1" == "--reset" ]; then
  docker compose down -v
fi

docker compose up -d
echo "Esperando a que MySQL esté listo..."
until docker exec cinemex-mysql mysqladmin ping -h 127.0.0.1 --silent 2>/dev/null; do
  sleep 2
done
echo "MySQL listo en localhost:3307 (base de datos: cinemex)"
