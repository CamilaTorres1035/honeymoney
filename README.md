<div align="center">

# HoneyMoney

### Expense Tracker API

API REST para la gestión de gastos personales con autenticación basada en JWT.
Diseñada con foco en seguridad, claridad de contratos y buenas prácticas REST.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=plastic&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=plastic&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=plastic&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=plastic&logo=jsonwebtokens&logoColor=white)
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

---

## Requisitos

Antes de ejecutar el proyecto, asegúrate de tener instalado:

- JDK 17
- Maven 3.x

---

## Quick Start

```bash
git clone https://github.com/CamilaTorres1035/honeymoney
cd honeymoney
mvn spring-boot:run
```

**Servidor disponible en:**

```
http://localhost:8080
```

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
- Filtros por fecha (última semana, mes, rango personalizado)
- Aislamiento por usuario (multi-user seguro)

---

## Tech Stack

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot |
| Seguridad | Spring Security |
| Autenticación | JWT |
| Base de datos | H2 (in-memory) |
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

**Ejemplos:**

```http
GET /api/expenses?range=last_week
GET /api/expenses?startDate=2026-06-01&endDate=2026-07-04
```

> **Nota:** todas las fechas deben enviarse en formato estándar `YYYY-MM-DD`.

---

## Consideraciones

- Todos los endpoints de gastos requieren autenticación
- No se expone información sobre recursos de otros usuarios
- Base de datos en memoria (no persistente para desarrollo)
- Formato de fechas: `YYYY-MM-DD`

---

## Documentación completa

Toda la definición de la API, contratos, catálogo de errores y reglas de negocio:

**Ver documentación completa:** [`/docs/api-spec.md`](/docs/api-spec.md)

---

## Testing

```bash
mvn test
```

---

## Roadmap

- [ ] Migrar a base de datos persistente
- [ ] Paginación en listados
- [ ] Filtro por categoría
- [ ] Refresh tokens (mejorar expiración de JWT)
- [ ] Dockerización
- [ ] Deploy

---

## Lecciones aplicadas

- Separación de responsabilidades por capas (Controller / Service / Security)
- Manejo de autenticación stateless
- Validación de inputs
- Diseño de APIs centradas en el usuario

---

## Autor

<div align="center">

**Camila Torres**

[![GitHub](https://img.shields.io/badge/GitHub-CamilaTorres1035-181717?style=plastic&logo=github&logoColor=white)](https://github.com/CamilaTorres1035)

</div>
