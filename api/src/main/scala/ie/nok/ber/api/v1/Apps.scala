package ie.nok.ber.api.v1

import ie.nok.ber.auth.middleware.UserRequestStoreAuthMiddleware

val apps =
  CertificateApp.http @@ UserRequestStoreAuthMiddleware.userRequestStoreAuthMiddleware ++
    SwaggerApp.http
