package ie.deed.ber.api.v1

import ie.deed.ber.auth.middleware.UserRequestAuthMiddleware

val apps =
  CertificateApp.http @@ UserRequestAuthMiddleware.userRequestAuthMiddleware ++
    SwaggerApp.http
