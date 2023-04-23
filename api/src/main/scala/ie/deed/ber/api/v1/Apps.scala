package ie.deed.ber.api.v1

import ie.deed.ber.auth.middleware.UserRequestStoreAuthMiddleware

val apps =
  CertificateApp.http @@ UserRequestStoreAuthMiddleware.userRequestStoreAuthMiddleware ++
    SwaggerApp.http
