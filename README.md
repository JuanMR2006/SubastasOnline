# Subastas en Línea

Proyecto académico en Java 17 + Spring Boot 3.3.4 (JPA/Hibernate, PostgreSQL, MapStruct 1.5.5).
Misma estructura y estilo que el repo base *HotelSpringBoot*, con el dominio del diagrama `Diagramas_Subastas_en_Linea.drawio`.

**Requisito central:** el vendedor ve **quiénes participan** en su subasta (`GET /api/subastas/{id}/participantes`)
y el administrador ve **qué subastas están ocurriendo** (`GET /api/subastas/activas`).

## Cómo ejecutarlo

1. Crear la base de datos en PostgreSQL (usuario `postgres`, clave `postgres`, puerto 5432):
   ```sql
   CREATE DATABASE subastas_db;
   ```
2. Ejecutar:
   ```bash
   ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
   ```
   Las tablas se crean solas (`ddl-auto=update`). API en `http://localhost:8080/api`.
3. Pruebas (no necesitan base de datos): `./mvnw test`

## Identidad sin login

No hay Spring Security. Los endpoints protegidos reciben el header `X-Usuario-Id` con el id del usuario.
Sin permiso responden **403**; si el usuario o el recurso no existe, **404**; reglas de negocio o datos inválidos, **400**.

## Endpoints

| Método | Ruta | Quién | Notas |
|---|---|---|---|
| POST | `/api/usuarios` | cualquiera | `rol` opcional (USUARIO por defecto) |
| GET | `/api/usuarios/{id}` | cualquiera | |
| POST | `/api/articulos/electronicos` | cualquiera | `duenoId` en el cuerpo |
| POST | `/api/articulos/coleccionables` | cualquiera | |
| GET | `/api/articulos/{id}` | cualquiera | |
| GET | `/api/articulos?duenoId=` | cualquiera | sin `duenoId` lista todos |
| POST | `/api/subastas` | cualquiera | 201; nace `PROGRAMADA` |
| GET | `/api/subastas/{id}` | cualquiera | oferta actual, tiempo restante, comisión si `FINALIZADA` |
| GET | `/api/subastas/activas` | **ADMINISTRADOR** (header) | resumen de subastas `ACTIVA` |
| POST | `/api/subastas/{id}/pujas` | cualquiera | 201; `postorId` y `monto` |
| GET | `/api/subastas/{id}/pujas` | cualquiera | historial |
| GET | `/api/subastas/{id}/participantes` | **vendedor o ADMINISTRADOR** (header) | por postor, ordenado por mejor oferta desc |
| PATCH | `/api/subastas/{id}/cancelar` | vendedor (sin pujas) o ADMINISTRADOR (header) | |
| POST | `/api/subastas/{id}/cerrar` | **ADMINISTRADOR** (header) | cierre manual para demostraciones |

## Demo rápida (curl)

```bash
# 1) Usuarios: 1=admin, 2=vendedor, 3 y 4=postores
curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' -d '{"nombre":"Admin","correo":"admin@mail.com","ciudad":"Pasto","rol":"ADMINISTRADOR"}'
curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' -d '{"nombre":"Juan","correo":"juan@mail.com","ciudad":"Pasto"}'
curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' -d '{"nombre":"Ana","correo":"ana@mail.com","ciudad":"Cali"}'
curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' -d '{"nombre":"Luis","correo":"luis@mail.com","ciudad":"Bogotá"}'

# 2) Juan registra su celular
curl -X POST localhost:8080/api/articulos/electronicos -H 'Content-Type: application/json' -d '{"nombre":"Celular A54","descripcion":"Como nuevo","duenoId":2,"marca":"Samsung","modelo":"A54","estadoUso":"Usado"}'

# 3) Juan crea la subasta. "inicio" no puede estar en el pasado: ponga 1-2 minutos adelante
#    (la tarea programada la activa sola, corre cada 60 s). "fin" unos minutos después.
curl -X POST localhost:8080/api/subastas -H 'Content-Type: application/json' -d '{"articuloId":1,"vendedorId":2,"precioBase":500000,"incrementoMinimo":50000,"inicio":"2026-10-06T20:00:00","fin":"2026-10-06T20:30:00"}'

# 4) Cuando esté ACTIVA, los postores ofertan
curl -X POST localhost:8080/api/subastas/1/pujas -H 'Content-Type: application/json' -d '{"postorId":3,"monto":500000}'
curl -X POST localhost:8080/api/subastas/1/pujas -H 'Content-Type: application/json' -d '{"postorId":4,"monto":550000}'
curl -X POST localhost:8080/api/subastas/1/pujas -H 'Content-Type: application/json' -d '{"postorId":3,"monto":880000}'

# 5) Requisito del profesor
curl localhost:8080/api/subastas/1/participantes -H 'X-Usuario-Id: 2'   # el vendedor ve quién participa
curl localhost:8080/api/subastas/activas -H 'X-Usuario-Id: 1'           # el admin ve las subastas en curso
curl localhost:8080/api/subastas/activas -H 'X-Usuario-Id: 3'           # 403: no es administrador

# 6) Cierre manual (admin) -> FINALIZADA, ganador Ana, comisión 5 % = 44000
curl -X POST localhost:8080/api/subastas/1/cerrar -H 'X-Usuario-Id: 1'
```

## Reglas de negocio (dentro de las entidades)

`Subasta.ofertar / activar / cerrar / cancelar / comision` y `Articulo.cambiarEstado` validan todo el dominio.
El polimorfismo se demuestra en `ArticuloPolimorfismoTest`: `Articulo.calcularComision()` invoca
`this.porcentajeComision()` y se ejecuta la versión del tipo real (electrónico 5 %, coleccionable 8 %).

## Suposiciones

- Los ids son `Long` autogenerados (el diagrama los define así; el repo base usa `UUID`).
- `Subasta.ofertaActual()` devuelve 0 cuando no hay pujas; la respuesta incluye `proximaOfertaMinima` (precio base si no hay pujas).
- Cualquiera puede registrarse con `rol=ADMINISTRADOR` (no hay login; es solo para demostración).
- Se usa bloqueo pesimista de la fila de la subasta al ofertar, cerrar y cancelar para evitar ofertas simultáneas inconsistentes.
- No se incluyó el test `contextLoads` del repo base porque exige PostgreSQL levantado.
