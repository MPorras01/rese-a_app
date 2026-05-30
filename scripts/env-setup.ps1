# PowerShell version of environment setup

param(
    [string]$Environment = "development"
)

switch ($Environment.ToLower()) {
    "development" {
        $env:VITE_API_URL = "http://localhost:8080"
        $env:VITE_ENVIRONMENT = "development"
        $env:VITE_LOG_LEVEL = "debug"
        $env:VITE_APP_NAME = "ReseñaApp"
        Write-Host "✓ Development environment loaded" -ForegroundColor Green
    }
    
    "staging" {
        $env:VITE_API_URL = "https://api-staging.resenaapp.com"
        $env:VITE_ENVIRONMENT = "staging"
        $env:VITE_LOG_LEVEL = "debug"
        $env:VITE_APP_NAME = "ReseñaApp Staging"
        Write-Host "✓ Staging environment loaded" -ForegroundColor Green
    }
    
    "production" {
        $env:VITE_API_URL = "https://api.resenaapp.com"
        $env:VITE_ENVIRONMENT = "production"
        $env:VITE_LOG_LEVEL = "info"
        $env:VITE_APP_NAME = "ReseñaApp"
        Write-Host "✓ Production environment loaded" -ForegroundColor Green
    }
    
    default {
        Write-Host "❌ Unknown environment: $Environment" -ForegroundColor Red
        Write-Host "Supported: development, staging, production"
        exit 1
    }
}

Write-Host ""
Write-Host "Environment Variables:" -ForegroundColor Cyan
Write-Host "  VITE_API_URL=$($env:VITE_API_URL)"
Write-Host "  VITE_ENVIRONMENT=$($env:VITE_ENVIRONMENT)"
Write-Host "  VITE_LOG_LEVEL=$($env:VITE_LOG_LEVEL)"
Write-Host "  VITE_APP_NAME=$($env:VITE_APP_NAME)"
