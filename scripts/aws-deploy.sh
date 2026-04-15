#!/bin/bash
# ─────────────────────────────────────────────────────────────
# aws-deploy.sh — Script de deploy/actualización en EC2
# Uso: ./scripts/aws-deploy.sh
# ─────────────────────────────────────────────────────────────
set -e

COMPOSE_FILE="docker-compose.aws.yml"
APP_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "📦 ReseñaApp — Deploy en AWS EC2"
echo "================================="

cd "$APP_DIR"

# 1. Verificar que .env existe
if [ ! -f ".env" ]; then
  echo "❌ Error: no se encontró .env"
  echo "   Copia .env.example a .env y configura las variables"
  exit 1
fi

# 2. Traer últimos cambios
echo "⬇️  Actualizando código..."
git pull origin "$(git branch --show-current)"

# 3. Build de la imagen
echo "🔨 Construyendo imagen Docker..."
docker compose -f "$COMPOSE_FILE" build --no-cache

# 4. Restart con zero-downtime básico
echo "🔄 Reiniciando contenedor..."
docker compose -f "$COMPOSE_FILE" up -d --force-recreate

# 5. Esperar health check
echo "⏳ Esperando que la app esté lista..."
MAX_WAIT=120
WAITED=0
until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; do
  if [ $WAITED -ge $MAX_WAIT ]; then
    echo "❌ Timeout: la app no respondió en ${MAX_WAIT}s"
    echo "   Ver logs: docker compose -f $COMPOSE_FILE logs app"
    exit 1
  fi
  sleep 5
  WAITED=$((WAITED + 5))
  echo "   Esperando... ${WAITED}s"
done

# 6. Limpiar imágenes viejas
echo "🧹 Limpiando imágenes antiguas..."
docker image prune -f

echo ""
echo "✅ Deploy completado exitosamente"
echo "   App corriendo en: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo 'localhost')"
echo "   Health: http://localhost:8080/actuator/health"
echo ""
echo "📋 Comandos útiles:"
echo "   Ver logs:    docker compose -f $COMPOSE_FILE logs -f app"
echo "   Estado:      docker compose -f $COMPOSE_FILE ps"
echo "   Reiniciar:   docker compose -f $COMPOSE_FILE restart app"
