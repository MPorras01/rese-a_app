# ReseñaApp

Estructura del repositorio:

- `backend/`: Spring Boot 3.2 (Java 21)
- `frontend/`: Vue 3 + Vite + TypeScript

## Desarrollo

Backend:

```powershell
cd backend
mvn spring-boot:run -DskipFrontend=true
```

Frontend:

```powershell
cd frontend
npm run dev
```

## Despliegue conjunto (un solo artefacto)

Compila frontend y backend, copia `frontend/dist` dentro de `backend/target/classes/static` y genera el jar:

```powershell
cd backend
mvn -DskipTests package
```

Jar generado:

- `backend/target/reviews-api-0.0.1-SNAPSHOT.jar`

## Opcional: omitir build de frontend desde Maven

```powershell
cd backend
mvn -DskipTests package -DskipFrontend=true
```
