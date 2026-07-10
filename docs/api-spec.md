# HoneyMoney – Expense Tracker API

Creado: 28 de junio de 2026 10:25
Etiquetas: APIs, Spring Boot

## Descripción

**HoneyMoney** es una API REST desarrollada con Spring Boot que permite a los usuarios gestionar sus gastos personales de manera segura y eficiente. En primera fase, la aplicación utilizará **H2 en modo in-memory** en lugar de una base de datos persistente, con el fin de para enfocarse en la lógica de negocio, autenticación y diseño de la API.

---

## Objetivo

Aplicar conceptos aprendidos para construir una API que permita a los usuarios registrar, consultar, actualizar y eliminar sus gastos, con autenticación basada en JWT para proteger los endpoints.

---

## Funcionalidades

### Autenticación y usuarios

- Registro de usuarios.
- Login con emisión de JWT.
- Validación de JWT para proteger los endpoints (mediante un middleware de seguridad en cada petición protegida).
- Identificación del usuario autenticado en cada petición a un endpoint protegido, a partir del JWT.

---

### Gestión de gastos

#### Crear gasto

- Permitir registrar un nuevo gasto asociado al usuario autenticado con:
    - Monto
    - Categoría
    - Descripción (opcional)
    - Fecha

#### Consultar un gasto específico

- Obtener un gasto específico del usuario autenticado.

#### Listar gastos

- Obtener todos los gastos del usuario autenticado.

#### Filtrar gastos

Permitir filtrar los gastos por rango de fechas:

- Última semana
- Último mes
- Últimos 3 meses
- Rango personalizado (`startDate` y `endDate`)

#### Actualizar gasto

- Modificar un gasto existente, todos los campos son actualizables, exceptuando el id y el usuario propietario.

#### Eliminar gasto

- Eliminar un gasto existente.

---

## Categorías de gastos

El sistema maneja las siguientes categorías predefinidas:

- Groceries
- Leisure
- Electronics
- Utilities
- Clothing
- Health
- Others

> Nota: en esta fase, las categorías se mantienen como un conjunto fijo utilizado internamente para validar los gastos creados/actualizados. No se expone un endpoint para consultarlas (`GET /api/categories` queda **fuera de alcance** en esta fase).
> 

---

## Restricciones técnicas

- JWT obligatorio para todos los endpoints protegidos, excepto `POST /api/auth/register` y `POST /api/auth/login`.
- Almacenamiento mediante H2 en modo in-memory (sin persistencia entre reinicios de la aplicación).
- Formato de fecha estándar para toda la API: `AAAA-MM-DD` (ISO 8601, ej. `2026-07-04`).
- Todo listado y filtrado de gastos (`GET /api/expenses` y sus variantes) debe estar acotado automáticamente al usuario autenticado identificado por el JWT.
- Paginación: **fuera de alcance** en esta fase.
- Filtro por categoría: **fuera de alcance** en esta fase (posible extensión futura).

---

## Modelo de Dominio

![image.png](image.png)

---

## Diseño de Endpoints

| Método | Endpoint | Función |
| --- | --- | --- |
| POST | /api/auth/register | Registro de usuarios |
| POST | /api/auth/login | Login de usuarios |
| POST | /api/expenses | Crear gasto |
| GET | /api/expenses/{id} | Obtener gasto especifico |
| GET | /api/expenses | Listar gastos del usuario autenticado |
| GET | /api/expenses?range=last_week | Filtrar por última semana |
| GET | /api/expenses?range=last_month | Filtrar por último mes |
| GET | /api/expenses?range=last_3_months | Filtrar por últimos 3 meses |
| GET | /api/expenses?startDate=2026-06-01&endDate=2026-07-04 | Filtrar por rango personalizado |
| PATCH | /api/expenses/{id} | Actualizar gasto |
| DELETE | /api/expenses/{id} | Eliminar gasto |

---

## Contratos de request-response (DTOs)

### Registro de Usuarios (`POST /api/auth/register`)

#### Request Body

```json
{
  "name": "Carlos Mendoza",
  "email": "carlos@example.com",
  "password": "PasswordSegura123!"
}
```

#### Response Body (201 Created)

```json
{
  "id": 1,
  "name": "Carlos Mendoza",
  "email": "carlos@example.com",
  "createdAt": "2026-07-04T16:06:00Z"
}
```

### Login de usuarios (**`POST /api/auth/login`**)

#### Request Body

```json
{
  "email": "carlos@example.com",
  "password": "PasswordSegura123!"
}
```

#### Resonse Body (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "Carlos Mendoza",
    "email": "carlos@example.com"
  }
}
```

### Crear gasto (**`POST /api/expenses`**)

#### Request Body

```json
{
  "amount": 45.50,
  "category": "Groceries",
  "description": "Cena de negocios",
  "expenseDate": "2026-07-03"
}
```

#### Response Body (201 Created)

```json
{
  "id": 1,
  "amount": 45.50,
  "category": "Groceries",
  "description": "Cena de negocios",
  "expenseDate": "2026-07-03",
  "createdAt": "2026-07-04T16:06:00Z",
  "updatedAt": "2026-07-04T16:06:00Z"
}
```

### Obtener gasto específico (`GET /api/expenses/{id}`)

#### Response Body (200 OK)

```json
{
  "id": 1,
  "amount": 45.50,
  "category": "Groceries",
  "description": "Cena de negocios",
  "expenseDate": "2026-07-03",
  "createdAt": "2026-07-04T16:06:00Z",
  "updatedAt": "2026-07-04T16:06:00Z"
}
```

### Listar / Filtrar gastos (**`GET /api/expenses`**)

*Aplica para listar todos, rangos predefinidos (`range`) y personalizados (`startDate`/`endDate`).*

#### Response Body (200 OK) - ejemplo con `*range*`

```json
{
  "data": [
    {
      "id": 1,
      "amount": 45.50,
      "category": "Groceries",
      "description": "Cena de negocios",
      "expenseDate": "2026-07-03",
      "createdAt": "2026-07-04T16:06:00Z",
      "updatedAt": "2026-07-04T16:06:00Z"
    },
    {
      "id": 2,
      "amount": 12.00,
      "category": "Leisure",
      "description": "Uber al trabajo",
      "expenseDate": "2026-06-28",
      "createdAt": "2026-06-28T09:15:00Z",
      "updatedAt": "2026-06-28T09:15:00Z"
    }
  ],
  "meta": {
    "totalCount": 2,
    "appliedFilters": {
      "range": "last_week",
      "startDate": null,
      "endDate": null
    }
  }
}
```

#### Response Body (200 OK) - ejemplo con rango personalizado

```json
{
  "data": [
    {
      "id": 1,
      "amount": 45.50,
      "category": "Groceries",
      "description": "Cena de negocios",
      "expenseDate": "2026-07-03",
      "createdAt": "2026-07-04T16:06:00Z",
      "updatedAt": "2026-07-04T16:06:00Z"
    }
  ],
  "meta": {
    "totalCount": 1,
    "appliedFilters": {
      "range": null,
      "startDate": "2026-06-01",
      "endDate": "2026-07-04"
    }
  }
}
```

### Actualizar gasto (**`PATCH /api/expenses/{id}`**)

#### Request Body

```json
// Solo Campos a Modificar
{
  "amount": 50.00,
  "description": "Cena de negocios (Ajuste de propina)"
}
```

#### Response Body (200 OK)

```json
{
  "id": 1,
  "amount": 50.00,
  "category": "Groceries",
  "description": "Cena de negocios (Ajuste de propina)",
  "expenseDate": "2026-07-03",
  "createdAt": "2026-07-04T16:06:00Z",
  "updatedAt": "2026-07-04T16:10:00Z"
}
```

### Eliminar gasto (**`DELETE /api/expenses/{id}`**)

#### Response Body (204 No Content)

> Se elige `204 No Content` sin cuerpo, en lugar de `200 OK` con JSON, por ser la convención REST más extendida para operaciones DELETE exitosas (el recurso ya no existe, por lo que no hay nada que devolver).
> 

---

## Catálogo de errores

#### `POST /api/auth/register`

| Código | Caso |
| --- | --- |
| 400 Bad Request | Campos faltantes, email con formato inválido, password que no cumple política mínima |
| 409 Conflict | El email ya está registrado |

#### `POST /api/auth/login`

| Código | Caso |
| --- | --- |
| 400 Bad Request | Campos faltantes (email o password vacíos) |
| 401 Unauthorized | Credenciales inválidas (email no existe o password incorrecto — mismo mensaje genérico para no revelar cuál de los dos falló) |

#### Todos los endpoints bajo `/api/expenses/**` (regla transversal)

| Código | Caso |
| --- | --- |
| 401 Unauthorized | Token ausente, inválido, malformado o expirado |

#### `POST /api/expenses`

| Código | Caso |
| --- | --- |
| 400 Bad Request | `amount` ausente, negativo o cero; `category` no corresponde a un valor del enum; `expenseDate` con formato distinto a `AAAA-MM-DD`; campos obligatorios faltantes |

#### `GET /api/expenses/{id}`

| Código | Caso |
| --- | --- |
| 404 Not Found | El gasto no existe, o existe pero pertenece a otro usuario (ver Decisión 2) |

#### `GET /api/expenses` (listado y filtros)

| Código | Caso |
| --- | --- |
| 400 Bad Request | Se envían simultáneamente `range` y (`startDate`/`endDate`) — regla ya definida |
| 400 Bad Request | Valor de `range` no reconocido (distinto de `last_week`, `last_month`, `last_3_months`) |
| 400 Bad Request | `startDate` o `endDate` con formato distinto a `AAAA-MM-DD` |
| 400 Bad Request | `startDate` posterior a `endDate` |

#### `PATCH /api/expenses/{id}`

| Código | Caso |
| --- | --- |
| 400 Bad Request | Algún campo enviado no cumple validación (`amount` ≤ 0, `category` inválida, `expenseDate` con formato incorrecto) |
| 404 Not Found | El gasto no existe o pertenece a otro usuario |

#### `DELETE /api/expenses/{id}`

| Código | Caso |
| --- | --- |
| 404 Not Found | El gasto no existe o pertenece a otro usuario |

#### Transversal a toda la API

| Código | Caso |
| --- | --- |
| 500 Internal Server Error | Error no controlado del servidor (fallback genérico, sin exponer detalles internos en `message`) |

---

## Reglas de negocio

### Regla de exclusión de filtros

`startDate`/`endDate` no pueden combinarse en la misma petición: si se reciben simultáneamente `range` y (`startDate` o `endDate`), la API responde `400 Bad Request` indicando que los parámetros son mutuamente excluyentes.