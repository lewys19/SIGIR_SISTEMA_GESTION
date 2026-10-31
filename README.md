# SIGIR — Sistema Integral de Gestión de Incidentes Regulatorios

Java 21 + Quarkus 3.40 · Hibernate Panache (Repository + DAO) · PostgreSQL 18 · Flyway · JWT RS256 · Redis Streams + WebSockets · React + Vite + Recharts

## Levantar todo con Docker
```sh
cp .env.example .env            # y cambia las contraseñas
./scripts/generar-claves.sh     # claves RSA del JWT (quedan en ./claves, fuera de git)
docker compose up -d --build
```
Abrir http://localhost:8180 · usuario `admin@sigir.pe` · contraseña = `SIGIR_ADMIN_PASSWORD` del `.env`.

## Desarrollo
```sh
# Backend (puerto 8090; Quarkus levanta su propio PostgreSQL en Docker)
cd backend && JAVA_HOME=~/.local/jdk-21 ./mvnw quarkus:dev     # admin@sigir.pe / Admin123!
# Tests
cd backend && JAVA_HOME=~/.local/jdk-21 ./mvnw test
# Frontend (puerto 5180, /api va al backend)
cd frontend && npm run dev
```

## Estructura (monolito modular por feature)
```
backend/src/main/java/pe/sigir/
  auth/           login, JWT, /me, cambiar mi contraseña
  usuarios/       alta, edición, reseteo de contraseña (DAO + repositorio Panache)
  roles/          matriz rol → permisos editable; PermisosAugmentor la aplica en cada petición
  auditoria/      bitácora síncrona en la misma transacción; trigger que impide UPDATE/DELETE
  incidentes/     registro, máquina de estados, plazo regulatorio según severidad
  revision/       aprobar / rechazar / cerrar, historial, segregación de funciones
  eventos/        eventos de dominio → Redis Stream (tras el commit) → WebSocket
  notificaciones/ bandeja por usuario + ticket de un uso para el WebSocket
  monitoreo/      últimos eventos del stream y estado del canal
  dashboard/      KPIs y series para gráficos
  reportes/       resumen por regulador/tipo, cumplimiento de plazos, exportación CSV
  compartido/     errores → HTTP, paginación, usuario actual
```

## Flujo de un incidente
`BORRADOR → EN_REVISION → APROBADO → CERRADO` (y `EN_REVISION → RECHAZADO → EN_REVISION`).
Solo el autor edita y envía; nadie aprueba ni rechaza su propio incidente.
Plazo para reportar al regulador desde la detección: Crítica 1 día · Alta 3 · Media 7 · Baja 15.

## Roles iniciales (editables en «Roles y permisos»)
| Rol | Puede |
|---|---|
| ADMIN | todo |
| SUPERVISOR | dashboard, incidentes, aprobar/rechazar/cerrar, monitoreo, reportes |
| ANALISTA | dashboard, registrar/editar/enviar incidentes |
| AUDITOR | dashboard, ver incidentes, monitoreo, reportes, auditoría |

## Tests
- Backend: 62 (unitarios de dominio + integración con PostgreSQL y Redis reales vía Dev Services)
- Frontend: `cd frontend && npm test` (Vitest)

## Sprints
- [x] Sprint 0 — esqueleto + login JWT
- [x] Los 10 módulos: Dashboard, Incidentes, Registro, Revisión/Aprobaciones, Monitoreo, Reportes, Usuarios, Roles/Permisos, Notificaciones, Auditoría
