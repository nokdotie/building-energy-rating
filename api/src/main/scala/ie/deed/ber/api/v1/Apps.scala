package ie.deed.ber.api.v1

import ie.deed.ber.auth.middleware.ApiKeyAuthMiddleware

val apps = CertificateApp.http @@ ApiKeyAuthMiddleware.apiKeyAuthMiddleware ++
  SwaggerApp.http
