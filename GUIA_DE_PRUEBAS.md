# Guía de Pruebas Locales - ReseñaApp

Fecha: 2026-06-12
Objetivo: levantar el proyecto y validar que backend, frontend, login y vistas principales funcionan correctamente.

## 1. Requisitos

- Windows con PowerShell
- Java 21+
- Maven 3.9+
- Node.js 22+
- Docker Desktop (recomendado)

Verifica versiones:

PowerShell:
java -version
mvn -version
node -v
npm -v
docker --version

## 2. Opción Recomendada: Docker Compose

Esta opción levanta base de datos y backend en contenedores. El frontend se sirve desde el backend en http://localhost:8080.

### 2.1 Levantar servicios

PowerShell:
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp
docker compose up -d

### 2.2 Verificar estado

PowerShell:
docker compose ps
docker compose logs app --tail 100

Criterio de éxito:
- El servicio db aparece healthy.
- El servicio app aparece running.

### 2.3 Probar salud de la API

PowerShell:
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/info

Criterio de éxito:
- health responde con status UP.
- info responde 200 OK.

## 3. Opción Desarrollo: DB en Docker + Backend/Frontend local

Usa esta opción si quieres hot reload del frontend y backend.

### 3.1 Levantar solo base de datos

PowerShell:
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp
docker compose up -d db

### 3.2 Levantar backend local

PowerShell (Terminal 2):
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp\backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

Criterio de éxito:
- Mensaje Started ReviewsApiApplication.
- API disponible en http://localhost:8080.

### 3.3 Levantar frontend local

PowerShell (Terminal 3):
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp\frontend
npm install
npm run dev

Criterio de éxito:
- Vite muestra la URL local.
- Frontend en http://localhost:5173.

## 4. Pruebas Funcionales Clave

## 4.1 Endpoints públicos

PowerShell:
Invoke-RestMethod http://localhost:8080/api/businesses

Criterio de éxito:
- Responde 200.
- Retorna lista/página de negocios.

## 4.2 Login con usuario demo

Credenciales demo:
- Email: admin@resena.local
- Password: Admin12345!

PowerShell:
$body = @{ email = 'admin@resena.local'; password = 'Admin12345!' } | ConvertTo-Json
$response = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -ContentType 'application/json' -Body $body
$response
$token = $response.token

Criterio de éxito:
- Responde 200.
- Retorna token JWT no vacío.

## 4.3 Endpoint protegido con JWT

PowerShell:
$headers = @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/reviews/mine -Headers $headers

Criterio de éxito:
- Responde 200.
- Retorna estructura paginada de reseñas (vacía o con datos).

## 5. Pruebas Visuales en Navegador

Abre:
- http://localhost:8080 si usaste Docker Compose
- http://localhost:5173 si usaste frontend local

Checklist visual:
- Home carga sin pantalla en blanco.
- Navbar visible.
- Lista de negocios renderiza.
- Navegación entre rutas (About/Contact/Login) sin recargar la página.
- Página de Login abre correctamente.
- Tras login, no hay errores visibles y la sesión se mantiene.

Recomendación:
- Abrir DevTools y revisar Console/Network.
- No deben aparecer errores 500 ni CORS.

## 6. Validación rápida de build

Backend:

PowerShell:
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp\backend
mvn clean package -DskipTests -DskipFrontend=true

Frontend:

PowerShell:
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp\frontend
npm run build

Criterio de éxito:
- Ambos comandos terminan sin errores.

## 7. Fallas comunes y solución

1. Docker no arranca
- Síntoma: error al ejecutar docker compose.
- Solución: abrir Docker Desktop y esperar estado Running.

2. Error de conexión a DB en backend
- Síntoma: HikariPool connection refused.
- Solución: verificar db en healthy con docker compose ps.

3. Frontend no conecta API
- Síntoma: errores 401/404/Network Error.
- Solución: confirmar backend activo en puerto 8080 y revisar URL de API en entorno.

4. Login falla con 401
- Síntoma: credenciales inválidas.
- Solución: usar admin@resena.local / Admin12345! y confirmar seed-admin habilitado en perfil dev.

## 8. Cierre y limpieza

Detener todo:

PowerShell:
cd c:\Users\porra\OneDrive\Documents\Repositorios\ReseñaApp
docker compose down

Si usaste backend/frontend locales, detener con Ctrl + C en cada terminal.

## 9. Resultado esperado final

La prueba se considera exitosa si:
- Health endpoint responde UP.
- Login devuelve JWT.
- Endpoint protegido responde con token.
- Frontend renderiza correctamente y navega sin errores.
- Build de backend y frontend completa sin fallos.
