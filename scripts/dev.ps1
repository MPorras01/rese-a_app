<#
.SYNOPSIS
  ReseñaApp — modo desarrollo con hot-reload completo.

.DESCRIPTION
  Levanta tres procesos:
    1. PostgreSQL en Docker           → localhost:5432
    2. Backend Spring Boot (DevTools) → localhost:8080  (reinicio automático al recompilar)
    3. Frontend Vite (HMR)            → localhost:5173  (actualización instantánea al guardar)

.USAGE
  .\scripts\dev.ps1
#>

$Root = Split-Path $PSScriptRoot -Parent
$Backend = Join-Path $Root "backend"
$Frontend = Join-Path $Root "frontend"

Write-Host ""
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  ReseñaApp — Modo Desarrollo con Hot-Reload" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Levantar solo la base de datos ─────────────────────────────────────────
Write-Host "[1/3] Iniciando PostgreSQL en Docker..." -ForegroundColor Yellow
Push-Location $Root
$composeFile = "docker-compose.dev.yml"

# Si el volumen externo aún no existe, usar el de docker-compose normal
$volumeExists = docker volume ls --format "{{.Name}}" | Where-Object { $_ -eq "reseaapp_pgdata" }
if (-not $volumeExists) {
    Write-Host "      Volumen 'reseaapp_pgdata' no encontrado; usando docker-compose.yml..." -ForegroundColor DarkYellow
    $composeFile = "docker-compose.yml"
    # Levantar solo el servicio db del compose principal
    docker compose -f docker-compose.yml up -d db 2>&1 | Out-Null
} else {
    docker compose -f $composeFile up -d 2>&1 | Out-Null
}
Pop-Location

# Esperar a que Postgres esté listo
Write-Host "      Esperando a que PostgreSQL este listo..."
$ready = $false
for ($i = 0; $i -lt 20; $i++) {
    $result = docker exec reseaapp-db-1 pg_isready -U resenias -d resenias 2>&1
    if ($result -match "accepting connections") {
        $ready = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    Write-Host "      ADVERTENCIA: No se pudo confirmar que Postgres este listo. Continuando de todas formas..." -ForegroundColor DarkYellow
}
Write-Host "      PostgreSQL listo en localhost:5432 ✓" -ForegroundColor Green

# ── 2. Backend: Spring Boot con DevTools ──────────────────────────────────────
Write-Host ""
Write-Host "[2/3] Iniciando backend Spring Boot (puerto 8080, hot-reload via DevTools)..." -ForegroundColor Yellow
$backendCmd = "Set-Location '$Backend'; " +
    "Write-Host 'Backend: http://localhost:8080' -ForegroundColor Green; " +
    "Write-Host 'Tip: guarda un .java -> Maven recompila -> Spring reinicia' -ForegroundColor DarkCyan; " +
    "Write-Host ''; " +
    "`$env:SPRING_PROFILES_ACTIVE = 'dev'; " +
    "mvn spring-boot:run -DskipFrontend=true '-Dspring-boot.run.profiles=dev'"

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    $backendCmd
) -WindowStyle Normal

# ── 3. Frontend: Vite con HMR ─────────────────────────────────────────────────
Write-Host "[3/3] Iniciando frontend Vite (puerto 5173, HMR activo)..." -ForegroundColor Yellow
$frontendCmd = "Set-Location '$Frontend'; " +
    "Write-Host 'Frontend: http://localhost:5173' -ForegroundColor Green; " +
    "Write-Host 'Tip: guarda cualquier .vue / .ts -> el navegador se actualiza solo' -ForegroundColor DarkCyan; " +
    "Write-Host ''; " +
    "npm run dev"

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    $frontendCmd
) -WindowStyle Normal

# ── Resumen ───────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Stack levantado!" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  App (modo dev):   http://localhost:5173" -ForegroundColor White
Write-Host "  API backend:      http://localhost:8080/api" -ForegroundColor White
Write-Host "  Health check:     http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "  Base de datos:    localhost:5432 / resenias" -ForegroundColor White
Write-Host ""
Write-Host "  Cuentas de prueba:" -ForegroundColor Cyan
Write-Host "    Admin:     admin@resena.local    / Admin12345!" -ForegroundColor White
Write-Host "    Dueno 1:   dueno1@resena.local   / Owner12345!" -ForegroundColor White
Write-Host "    Dueno 2:   dueno2@resena.local   / Owner12345!" -ForegroundColor White
Write-Host "    Cliente 1: cliente1@resena.local / Client12345!" -ForegroundColor White
Write-Host "    Cliente 2: cliente2@resena.local / Client12345!" -ForegroundColor White
Write-Host "    Cliente 3: cliente3@resena.local / Client12345!" -ForegroundColor White
Write-Host ""
Write-Host "  Hot-reload:" -ForegroundColor Cyan
Write-Host "    Frontend: guarda cualquier .vue / .ts -> HMR instantaneo" -ForegroundColor White
Write-Host "    Backend:  guarda .java -> recompila con 'mvn compile'" -ForegroundColor White
Write-Host "              (en otra terminal: cd backend && mvn compile)" -ForegroundColor DarkGray
Write-Host ""
Write-Host "  Para detener todo:" -ForegroundColor Cyan
Write-Host "    Cierra las ventanas de terminal abiertas y ejecuta:" -ForegroundColor White
Write-Host "    docker compose -f docker-compose.dev.yml down" -ForegroundColor DarkGray
Write-Host ""
