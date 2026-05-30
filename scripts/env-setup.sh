#!/bin/bash
# Environment configuration for ReseñaApp
# Supports: development, staging, production

ENVIRONMENT=${1:-development}

case $ENVIRONMENT in
  development|dev)
    export VITE_API_URL="http://localhost:8080"
    export VITE_ENVIRONMENT="development"
    export VITE_LOG_LEVEL="debug"
    export VITE_APP_NAME="ReseñaApp"
    echo "✓ Development environment loaded"
    ;;
    
  staging|stage)
    export VITE_API_URL="https://api-staging.resenaapp.com"
    export VITE_ENVIRONMENT="staging"
    export VITE_LOG_LEVEL="debug"
    export VITE_APP_NAME="ReseñaApp Staging"
    echo "✓ Staging environment loaded"
    ;;
    
  production|prod)
    export VITE_API_URL="https://api.resenaapp.com"
    export VITE_ENVIRONMENT="production"
    export VITE_LOG_LEVEL="info"
    export VITE_APP_NAME="ReseñaApp"
    echo "✓ Production environment loaded"
    ;;
    
  *)
    echo "❌ Unknown environment: $ENVIRONMENT"
    echo "Supported: development, staging, production"
    exit 1
    ;;
esac

echo ""
echo "Environment Variables:"
echo "  VITE_API_URL=$VITE_API_URL"
echo "  VITE_ENVIRONMENT=$VITE_ENVIRONMENT"
echo "  VITE_LOG_LEVEL=$VITE_LOG_LEVEL"
echo "  VITE_APP_NAME=$VITE_APP_NAME"
