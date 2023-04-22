package ie.deed.ber.auth.middleware

import ie.deed.ber.auth.model.{ApiKey, ApiKeyType, UserRequest}
import ie.deed.ber.auth.store.{ApiKeyStore, UserRequestStore}
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.RequestHandlerMiddleware
import java.time.Instant

object UserRequestAuthMiddleware {

  val userRequestAuthMiddleware
  : RequestHandlerMiddleware[Nothing, ApiKeyStore with UserRequestStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header("X-API-Key").map(_.value.toString)
      val apiKeyZIO = ApiKeyStore.getApiKey(maybeHeader.getOrElse(""))
      apiKeyZIO.map {
        case Some(ApiKey(email, _, ApiKeyType.User, _)) => UserRequestStore.saveUserRequest(UserRequest(email, Instant.now(), ""))
      }
      apiKeyZIO.map(_.nonEmpty)
    })
}
