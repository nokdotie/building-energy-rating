package ie.deed.ber.auth.middleware

import ie.deed.ber.auth.store.UserApiKeyStore
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.RequestHandlerMiddleware

object ApiKeyAuthMiddleware {

  val apiKeyAuthMiddleware
      : RequestHandlerMiddleware[Nothing, UserApiKeyStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header("X-API-Key").map(_.value.toString)
      UserApiKeyStore.isValidApiKey(maybeHeader.getOrElse(""))
    })
}
