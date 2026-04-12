#!/bin/bash
# Deploy script for Dokploy
# Usage: ./deploy.sh [environment]

set -e

ENVIRONMENT=${1:-production}
DOCKER_REGISTRY=${DOCKER_REGISTRY:-ghcr.io}
IMAGE_NAME="$DOCKER_REGISTRY/$(git config --get remote.origin.url | sed 's/.git$//' | awk -F'/' '{print $(NF-1) "/" $NF}')"
IMAGE_TAG=$(git describe --tags --always)
TIMESTAMP=$(date +%s)

echo "🚀 Desplegando ReseñaApp a $ENVIRONMENT"
echo "📦 Imagen: $IMAGE_NAME:$IMAGE_TAG"
echo "🕐 Timestamp: $TIMESTAMP"

# 1. Validar configuración
echo "✓ Validando configuración..."
if [ ! -f ".env" ]; then
    echo "❌ Archivo .env no encontrado. Copiar .env.example"
    cp .env.example .env
    echo "⚠️  Actualizar .env con valores reales"
    exit 1
fi

# 2. Validar credenciales
echo "✓ Validando credenciales..."
if [ -z "$JWT_SECRET" ] || [ ${#JWT_SECRET} -lt 32 ]; then
    echo "⚠️  JWT_SECRET no definido o muy corto. Generando..."
    JWT_SECRET=$(openssl rand -base64 64)
    echo "Nueva JWT_SECRET: $JWT_SECRET"
fi

# 3. Build Docker image
echo "🔨 Compilando imagen Docker..."
docker build \
    --tag "$IMAGE_NAME:$IMAGE_TAG" \
    --tag "$IMAGE_NAME:latest" \
    --label "version=$IMAGE_TAG" \
    --label "timestamp=$TIMESTAMP" \
    --label "environment=$ENVIRONMENT" \
    .

# 4. Push to registry
if [ "$DOCKER_REGISTRY" != "local" ]; then
    echo "📤 Subiendo imagen al registro..."
    docker push "$IMAGE_NAME:$IMAGE_TAG"
    docker push "$IMAGE_NAME:latest"
fi

# 5. Deploy con docker-compose
echo "🚢 Desplegando contenedores..."
if [ "$ENVIRONMENT" = "production" ]; then
    docker-compose -f docker-compose.prod.yml up -d
else
    docker-compose up -d
fi

# 6. Esperar a que la app esté healthy
echo "⏳ Esperando a que la aplicación sea saludable..."
RETRIES=30
DELAY=2
for i in $(seq 1 $RETRIES); do
    if docker compose exec app curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Aplicación está saludable"
        break
    fi
    echo "Intento $i/$RETRIES..."
    sleep $DELAY
    if [ $i -eq $RETRIES ]; then
        echo "❌ Timeout esperando que la app sea saludable"
        exit 1
    fi
done

# 7. Mostrar estado
echo ""
echo "✓ Despliegue completado!"
echo ""
echo "📊 Estado de contenedores:"
docker compose ps

echo ""
echo "🌐 Acceder a la aplicación:"
if [ "$ENVIRONMENT" = "production" ]; then
    echo "  → Frontend: https://$(grep FRONTEND_URL .env | cut -d'=' -f2)"
    echo "  → API: https://$(grep FRONTEND_URL .env | cut -d'=' -f2)/api"
else
    echo "  → Frontend: http://localhost:8080"
    echo "  → API: http://localhost:8080/api"
fi

echo ""
echo "📋 Ver logs:"
echo "  → docker compose logs -f app"

echo ""
echo "🛑 Para detener:"
echo "  → docker compose down"
