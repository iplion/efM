### Здравсвуйте! Высылаю то, что успел сделать за отведенное время. Я очень старался!

# Cards API

REST API для управления банковскими картами.

## Стек

- Java 17
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Liquibase
- Docker Compose
- Swagger UI / OpenAPI

## Запуск dev-среды

```bash
docker compose up -d
```

```bash
./mvnw spring-boot:run
```

Приложение стартует на `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI-файл:

```text
docs/openapi.yaml
```

## Dev-пользователи

Seed-данные подключены через Liquibase context `dev`.

```text
admin / admin
user / user
```

## Аутентификация

Получить JWT:

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "login": "admin",
  "password": "admin"
}
```

Ответ:

```json
{
  "jwtToken": "..."
}
```

Дальше передавать токен:

```http
Authorization: Bearer <jwtToken>
```

## Основные endpoints

### ADMIN

- `POST /api/v1/admin/users` — создать пользователя
- `GET /api/v1/admin/users` — список пользователей
- `GET /api/v1/admin/users/{id}` — получить пользователя
- `PUT /api/v1/admin/users/{id}` — обновить пользователя
- `PATCH /api/v1/admin/users/{id}/enable` — включить пользователя
- `PATCH /api/v1/admin/users/{id}/disable` — отключить пользователя
- `POST /api/v1/admin/cards` — создать карту
- `GET /api/v1/admin/cards` — список всех карт
- `GET /api/v1/admin/cards/{publicId}` — получить карту
- `PUT /api/v1/admin/cards/{publicId}` — обновить карту
- `PATCH /api/v1/admin/cards/{publicId}/activate` — активировать карту
- `PATCH /api/v1/admin/cards/{publicId}/block` — заблокировать карту
- `DELETE /api/v1/admin/cards/{publicId}` — удалить карту
- `GET /api/v1/card-block-requests/requested` — активные заявки на блокировку
- `PATCH /api/v1/card-block-requests/{requestId}/approve` — подтвердить заявку
- `PATCH /api/v1/card-block-requests/{requestId}/reject` — отклонить заявку

### USER

- `GET /api/v1/cards` — свои карты, фильтрация по статусу и пагинация
- `GET /api/v1/cards/{publicId}` — своя карта
- `GET /api/v1/cards/{publicId}/balance` — баланс карты
- `POST /api/v1/card-block-requests/cards/{cardPublicId}` — запросить блокировку
- `GET /api/v1/card-block-requests/my` — свои заявки
- `POST /api/v1/transfers` — перевод между своими картами
- `GET /api/v1/transfers` — история своих переводов

Для `POST /api/v1/transfers` поле `publicId` в теле запроса является идемпотентным ключом операции. При повторной отправке того же `publicId` деньги повторно не списываются, API возвращает ошибку `409 Conflict`.

## Безопасность карт

Номер карты не хранится открытым текстом:

- `encrypted_card_number` — AES-GCM;
- `card_number_hash` — HMAC-SHA256 для уникальности;
- наружу отдаётся только маска вида `**** **** **** 1234`.

Ключи задаются переменными окружения:

```text
CARD_ENCRYPTION_KEY
CARD_HMAC_KEY
JWT_SECRET
```

## Проверка

```bash
./mvnw test
```

```bash
./mvnw -DskipTests compile
```
