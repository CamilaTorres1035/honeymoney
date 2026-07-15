<div align="center">

# HoneyMoney
### Expense Tracker API

API REST para la gestión de gastos personales con autenticación basada en JWT. Diseñada con foco en seguridad, claridad de contratos y buenas prácticas REST.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=plastic&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=plastic&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=plastic&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=plastic&logo=jsonwebtokens&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=plastic&logo=supabase&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=plastic&logo=apachemaven&logoColor=white)

</div>

---

## ¿Por qué existe este proyecto?

Este proyecto fue construido para aplicar conceptos clave de backend:

| Concepto | Aplicación en el proyecto |
|---|---|
| Diseño de APIs REST | Recursos, verbos y códigos de estado consistentes |
| Autenticación y autorización | JWT con rutas protegidas |
| Manejo de errores | Formato de error unificado |
| Modelado de dominio | Entidades y relaciones claras |
| Validación de datos | Reglas de negocio en capa de servicio |
| Persistencia | Cambio de base de datos local a una instancia en la nube con Supabase |

---

## Requisitos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- JDK 17
- Maven 3.x
- Una cuenta activa en Supabase con un proyecto creado

---

## Quick Start

```bash
git clone https://github.com/CamilaTorres1035/honeymoney
cd honeymoney
```

Configura las variables de entorno necesarias (ver [Variables de entorno](#variables-de-entorno)), asegurándote de definir `PASS_SP_DB` con la contraseña de tu base de datos en Supabase. Puedes copiar `.env.example` como referencia.

```bash
mvn spring-boot:run
```

**Servidor disponible en:**

```
http://localhost:8080
```

**Documentación interactiva (Swagger UI):**

```
http://localhost:8080/swagger-ui/index.html
```

---

## Variables de entorno

| Variable | Descripción | Obligatoria |
|---|---|---|
| `PASS_SP_DB` | Contraseña de la base de datos PostgreSQL de Supabase | Sí |
| `JWT_SECRET` | Clave para firmar los JWT (HS256, base64 ≥ 256 bits) | Recomendada (tiene default solo para desarrollo local) |
| `JWT_EXPIRATION` | Expiración del token en milisegundos (default: `3600000` = 1h) | No |

> Ver `.env.example` para valores de referencia. Nunca commitees un `.env` real con secretos.

---

## Autenticación

La API utiliza **JWT (JSON Web Tokens)** para proteger sus rutas.

### Flujo de autenticación

1. Registrar usuario en `POST /api/auth/register`
2. Iniciar sesión en `POST /api/auth/login`
3. Usar el token recibido en cada request protegido:

```
Authorization: Bearer <token>
```

---

## Features

- Registro y login de usuarios
- Autenticación con JWT
- CRUD completo de gastos
- Filtros por fecha (última semana, mes, últimos 3 meses, rango personalizado)
- Filtro por categoría, combinable con los filtros de fecha
- Catálogo de categorías vía endpoint público
- Paginación en el listado de gastos
- Aislamiento por usuario (multi-user seguro)
- Documentación interactiva vía Swagger / OpenAPI

---

## Tech Stack

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot |
| Seguridad | Spring Security |
| Autenticación | JWT |
| Base de datos | Supabase (PostgreSQL Cloud) |
| Documentación API | springdoc-openapi (Swagger UI) |
| Build tool | Maven |

---

## Endpoints disponibles

### Auth

| Método | Endpoint |
|---|---|
| `POST` | `/api/auth/register` |
| `POST` | `/api/auth/login` |

### Expenses

| Método | Endpoint |
|---|---|
| `GET` | `/api/expenses` |
| `POST` | `/api/expenses` |
| `GET` | `/api/expenses/{id}` |
| `PATCH` | `/api/expenses/{id}` |
| `DELETE` | `/api/expenses/{id}` |

#### Filtros soportados

| Parámetro | Valores / Formato |
|---|---|
| `range` | `last_week` \| `last_month` \| `last_3_months` |
| `startDate` / `endDate` | `YYYY-MM-DD` |
| `category` | Una de las categorías del catálogo (ver abajo). Combinable con `range` o con `startDate`/`endDate` |
| `page` / `size` | Paginación estándar (default: `page=0`, `size=10`) |

**Ejemplos:**

```http
GET /api/expenses?range=last_week
GET /api/expenses?startDate=2026-06-01&endDate=2026-07-04
GET /api/expenses?category=Groceries
GET /api/expenses?category=Groceries&range=last_month
GET /api/expenses?page=0&size=2
```

> **Nota:** `range` y `startDate`/`endDate` son mutuamente excluyentes. Todas las fechas deben enviarse en formato estándar `YYYY-MM-DD`.

### Categories

| Método | Endpoint |
|---|---|
| `GET` | `/api/categories` |

Endpoint público (no requiere token). Devuelve el catálogo fijo de categorías: `Groceries`, `Leisure`, `Electronics`, `Utilities`, `Clothing`, `Health`, `Others`.

---

## Consideraciones

- Todos los endpoints de gastos requieren autenticación
- No se expone información sobre recursos de otros usuarios
- Base de datos en la nube autogestionada (Supabase)
- Formato de fechas: `YYYY-MM-DD`

---

## Documentación completa

Toda la definición de la API, contratos, catálogo de errores y reglas de negocio:

**Ver documentación completa:** [`/docs/api-spec.md`](/docs/api-spec-(V2).md)

---

## Testing

```bash
mvn test
```

---

## Roadmap

- [x] Migrar a base de datos persistente en la nube (Supabase)
- [x] Paginación en listados
- [x] Filtro por categoría
- [x] Endpoint de categorías
- [ ] Refresh tokens (mejorar expiración de JWT)
- [ ] Dockerización
- [ ] Deploy

---

## Lecciones aplicadas

- Separación de responsabilidades por capas (Controller / Service / Security)
- Manejo de autenticación stateless
- Validación de inputs
- Diseño de APIs centradas en el usuario
- Migración de un almacenamiento in-memory a una base de datos persistente
- Conexión y persistencia con un proveedor de bases de datos Cloud (Supabase)

---

## Autor

<div align="center">

**Camila Torres**

[![GitHub](https://img.shields.io/badge/GitHub-CamilaTorres1035-181717?style=plastic&logo=github&logoColor=white)](https://github.com/CamilaTorres1035)

</div>
