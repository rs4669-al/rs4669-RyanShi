# Tax API Service

Individual Project for COMS W4156 Advanced Software Engineering, Fall 2026.

This repository contains a Spring Boot REST API for managing clients and items and calculating sales-tax quotes. Assignment 3 adds GitHub Actions CI, expanded automated tests, coverage enforcement, and a small state-tax comparison client application.

## Requirements

- Java 17
- Git
- Internet access the first time Maven downloads dependencies

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

## Project layout

```text
.
├── .github/workflows/ci.yml
├── honesty.txt
├── citations.txt
├── bugs.txt
├── README.md
└── IndividualProject/
    ├── pom.xml
    ├── mvnw
    └── src/
        ├── main/java/com/taxapi/
        └── main/resources/
            ├── data/
            └── static/index.html
```

## Setup

Clone the repository and enter the repository directory:

```bash
git clone <https://github.com/rs4669-al/rs4669-RyanShi.git>
cd rs4669-RyanShi
```

The API project is inside `IndividualProject`.

## Run the application

From the repository root:

```bash
cd IndividualProject
./mvnw spring-boot:run
```

The Spring Boot server runs at:

```text
http://localhost:8080
```

The client application is served from the same Spring Boot server. Open:

```text
http://localhost:8080/
```

The application uses the local JSON files in `src/main/resources/data/` for its local storage configuration.

## Test the project

Run the complete test suite:

```bash
cd IndividualProject
./mvnw clean test
```

Run Checkstyle:

```bash
./mvnw checkstyle:check
```

Run the full verification, including the JaCoCo branch-coverage requirement:

```bash
./mvnw clean verify
```

The project requires at least 85% branch coverage. JaCoCo generates its report at:

```text
IndividualProject/target/site/jacoco/index.html
```

The CI workflow also runs the Maven tests, Checkstyle, and coverage verification on every push and pull request.

## API authentication

Protected endpoints require the `X-API-Key` request header. Local example client keys are stored in:

```text
IndividualProject/src/main/resources/data/clients.json
```

For example:

```bash
-H "X-API-Key: 540ba996-2ae9-4f72-8b4f-014b463bc4d7"
```

These keys are local demo data only.

## API endpoints

All API endpoints are under `/v1`.

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/v1/clients` | Creates a new client. Duplicate names return `409 Conflict`. | No |
| POST | `/v1/items` | Creates an item with a name, category, and base price. | `X-API-Key` |
| GET | `/v1/items` | Returns all items. Optional `category` and `q` filters can be supplied. | `X-API-Key` |
| GET | `/v1/items/{id}` | Returns one item by ID. | `X-API-Key` |
| PATCH | `/v1/items/{id}` | Updates only an item's `basePrice`. | `X-API-Key` |
| DELETE | `/v1/items/{id}` | Deletes an item by ID. | `X-API-Key` |
| POST | `/v1/tax/quote` | Calculates a tax quote using either an item ID or a direct price/category. | `X-API-Key` |
| GET | `/v1/supported` | Returns the states and categories represented by the tax-rate data. | `X-API-Key` |

### POST `/v1/clients`

Request:

```json
{
  "name": "Example Client"
}
```

A new client receives a generated ID and API key. If the name already exists, the service returns `409 Conflict`.

### POST `/v1/items`

Request:

```json
{
  "name": "Laptop",
  "category": "electronics",
  "basePrice": 999.99
}
```

### GET `/v1/items`

Without filters:

```bash
curl -H "X-API-Key: YOUR_API_KEY" http://localhost:8080/v1/items
```

Optional filters:

```text
/v1/items?category=clothing
/v1/items?q=laptop
/v1/items?category=clothing&q=shirt
```

### GET `/v1/items/{id}`

Returns the item whose ID matches the path parameter. A missing item returns `404 Not Found`.

### PATCH `/v1/items/{id}`

Only the base price is changed. The item's ID, name, and category remain unchanged.

Request:

```json
{
  "basePrice": 1099.99
}
```

Example:

```bash
curl -X PATCH \
  -H "X-API-Key: YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"basePrice":1099.99}' \
  http://localhost:8080/v1/items/ITEM_ID
```

### DELETE `/v1/items/{id}`

Deletes an item and returns `204 No Content` when successful. A missing item returns `404 Not Found`.

### POST `/v1/tax/quote`

The request can use an existing item:

```json
{
  "itemId": "ITEM_ID",
  "state": "CA"
}
```

Or it can provide a price and category directly:

```json
{
  "price": 100.00,
  "category": "clothing",
  "state": "CA"
}
```

The response contains the pre-tax price, tax rate, tax amount, and total:

```json
{
  "price": 100.0,
  "taxRate": 0.0725,
  "taxAmount": 7.25,
  "total": 107.25
}
```

### GET `/v1/supported`

Returns the distinct states and categories available in the local tax-rate data.

```bash
curl -H "X-API-Key: YOUR_API_KEY" http://localhost:8080/v1/supported
```

## Client application

Assignment 3 includes a small **State Tax Comparison** client application. It is served by the same Spring Boot application at `http://localhost:8080/` and calls the Tax API service directly from the browser.

The client uses:

- `GET /v1/supported` to load the available states and categories.
- `POST /v1/tax/quote` to calculate the selected tax quote.

The client application was built with **OpenAI ChatGPT**, as permitted by the Assignment 3 instructions.

For a demo, a non-zero combination such as California + clothing with a $100 price makes the API result easy to see.

## Demo videos

The assignment requires two videos, each no longer than three minutes, but I made all into 1:

https://youtu.be/mVjkSQbOM7E

### Suggested API demo

Run the application and show two endpoints, for example:

1. `GET /v1/items` with a valid API key.
2. `POST /v1/tax/quote` using California + clothing and a price such as `$100`.

### Suggested client demo

1. Open `http://localhost:8080/`.
2. Show the state/category choices loading from `/v1/supported`.
3. Enter a price.
4. Click **Calculate tax**.
5. Show the returned tax rate, tax amount, and total. This demonstrates the client calling `/v1/tax/quote`.

## Continuous integration

The workflow is located at `.github/workflows/ci.yml`.

It runs on every push and pull request and performs:

1. Checkout of the repository.
2. Java 17 setup.
3. Maven dependency caching.
4. Unit/integration tests.
5. Checkstyle verification.
6. JaCoCo branch-coverage verification at a minimum of 85%.

A failed test, Checkstyle check, or coverage requirement causes the workflow to fail.

## Assignment documentation

- `honesty.txt` contains the academic-honesty statement for this assignment.
- `citations.txt` lists outside technical sources and AI assistance used for Assignment 3.
- `bugs.txt` records the bugs reviewed/resolved during the project.
