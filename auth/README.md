### Auth overview

Authentication is based on the UserToken.
Every request has to send a valid token in the `X-API-Key` header.

### Types of implementation:

- local: `UserTokenInMemoryStore`
- dev/prod: `GoogleFirestoreUserTokenStore`

**NOTICE:**  To avoid frequent DB calls please use `UserTokenInMemoryStore` for local development.

