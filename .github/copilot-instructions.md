# Instrucciones de GitFlow del proyecto

Este repositorio usa siempre estas ramas base:

- `master`
- `qa`
- `dev`
- `release`

Reglas obligatorias:

1. Toda rama `feature/*` se crea desde `master`.
2. Toda feature debe abrir PR y pasar primero a `dev`.
3. Luego se promueve de `dev` a `release`.
4. Desde `release` se promueve a `qa` para validación.
5. Desde `release` se promueve a `master` para producción.
6. No se permiten promociones directas que salten el flujo anterior.
7. Todo cambio debe ir por Merge Request con aprobación explícita.
8. Cada Merge Request debe asociarse a una HU/issue en GitHub.

Secuencia oficial de promoción:

- `feature/*` -> `dev`
- `dev` -> `release`
- `release` -> `qa`
- `release` -> `master`
