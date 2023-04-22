package ie.deed.ber.auth.middleware

import ie.deed.ber.auth.model.ApiKey
import ie.deed.ber.auth.model.UserRequest
import ie.deed.ber.auth.store.{ApiKeyStore, UserRequestStore}
import zio.Trace
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.model.Status
import zio.http.{Handler, Request, RequestHandlerMiddleware, Response}
import java.time.Instant

object ApiKeyAuthMiddleware {

  val apiKeyAuthMiddleware
      : RequestHandlerMiddleware[Nothing, ApiKeyStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header("X-API-Key").map(_.value.toString)
      ApiKeyStore.isValidApiKey(maybeHeader.getOrElse(""))
    })
}
