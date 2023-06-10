package ie.nok.ber.auth.middleware

import ie.nok.ber.auth.model.ApiKey
import ie.nok.ber.auth.model.UserRequest
import ie.nok.ber.auth.store.{ApiKeyStore, UserRequestStore}
import zio.{Trace, ZIO}
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.model.Status
import zio.http.{Handler, Request, RequestHandlerMiddleware, Response}

import java.time.Instant

object ApiKeyAuthMiddleware {

  val apiKeyAuthMiddleware
      : RequestHandlerMiddleware[Nothing, ApiKeyStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header(Headers.ApiKey).map(_.value.toString)
      ApiKeyStore.isValidApiKey(maybeHeader.getOrElse(""))
    })
}
