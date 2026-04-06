# MirSanLab Backend

API REST para gestion de:
- autenticacion de usuarios (JWT)
- turnos de laboratorio
- resultados de estudios en PDF

## Stack
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MySQL 8
- Docker / Docker Compose

## Estructura funcional
- `ADMIN`: gestiona turnos, sube resultados, busca usuarios.
- `PACIENTE`: registra/login, reserva/cancela turnos propios, ve/descarga resultados propios.

## Configuracion local
1. Copiar variables de ejemplo:
```bash
cp .env.example .env
```
En Windows PowerShell:
```powershell
Copy-Item .env.example .env
```

2. Completar `.env` con valores reales:
- DB (`MYSQL_*`)
- JWT (`JWT_SECRET`)
- SMTP (`SPRING_MAIL_*`)
- bootstrap admin (`BOOTSTRAP_ADMIN_*`)

3. Levantar:
```bash
docker compose up --build
```

## Bootstrap del admin (primer arranque)
La app puede crear un admin automaticamente solo la primera vez.

Variables:
- `BOOTSTRAP_ADMIN_ENABLED=true`
- `BOOTSTRAP_ADMIN_EMAIL`
- `BOOTSTRAP_ADMIN_PASSWORD`
- `BOOTSTRAP_ADMIN_NAME` (opcional)
- `BOOTSTRAP_ADMIN_PHONE` (opcional)

Comportamiento:
- si ya existe un usuario con rol `ADMIN`, no crea otro.
- recomendado: luego del primer deploy cambiar `BOOTSTRAP_ADMIN_ENABLED=false`.

## Autenticacion
- `POST /api/auth/login` devuelve:
```json
{
  "token": "jwt...",
  "rol": "ADMIN",
  "nombre": "Administrador"
}
```

Usar el token en endpoints protegidos:
```http
Authorization: Bearer <TOKEN>
```

## Endpoints

### Auth
- `POST /api/auth/register`
  - publico
  - body:
```json
{
  "nombre": "Juan Perez",
  "email": "juan@mail.com",
  "password": "Clave123",
  "telefono": "3624000000"
}
```
- `POST /api/auth/login`
  - publico
  - body:
```json
{
  "email": "admin@lab.com",
  "password": "Clave123"
}
```

### Turnos
- `GET /api/turnos?page=0&size=7`
  - requiere token
  - devuelve disponibilidad por dia.

- `POST /api/turnos`
  - requiere token (paciente)
  - body:
```json
{
  "fecha": "2026-04-10",
  "hora": "09:00:00"
}
```

- `GET /api/turnos/pendientes?page=0&size=10`
  - requiere token admin

- `PUT /api/turnos/{id}/realizar`
  - requiere token admin

- `PUT /api/turnos/{id}/cancelar`
  - requiere token admin

- `PUT /api/turnos/{id}/cancelar-paciente`
  - requiere token paciente

- `GET /api/turnos/proximo`
  - requiere token paciente

### Resultados
- `POST /api/resultados`
  - requiere token admin
  - `multipart/form-data`
  - siempre incluir `archivo` (PDF)
  - ademas uno de estos esquemas:
    - `pacienteId` + `archivo`
    - `emailDestino` + `archivo`
    - `correoDestino` + `archivo`

- `GET /api/resultados?page=0&size=10`
  - requiere token paciente

- `GET /api/resultados/{id}/descargar`
  - requiere token paciente
  - descarga/inline del PDF.

### Usuarios
- `GET /api/usuarios/buscar?query=juan`
  - requiere token admin

## Postman
Se incluye la coleccion lista para importar:
- `collection.json`

Variables de coleccion esperadas:
- `baseUrl` (default: `http://localhost:8080`)
- `adminToken`
- `pacienteToken`
- `pacienteId`
- `turnoId`
- `resultadoId`
- `emailDestino`

## Seguridad recomendada antes de publicar repo
- no versionar `.env`
- no hardcodear secretos en `docker-compose.yml`
- rotar secretos si alguna vez se expusieron (JWT, SMTP, DB)

