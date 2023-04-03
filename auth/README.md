### Auth overview

Authentication is based on the ApiKey.
Every request has to send a valid apiKey in the `X-API-Key` header.

### Types of implementation:

- local: `ApiKeyInMemoryStore`
- dev/prod: `GoogleFirestoreApiKeyStore`

**NOTICE:**  To avoid frequent DB calls please use `ApiKeyInMemoryStore` for local development.

