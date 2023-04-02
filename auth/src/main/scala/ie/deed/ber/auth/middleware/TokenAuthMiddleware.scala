package ie.deed.ber.auth.middleware

import ie.deed.ber.auth.store.UserTokenStore
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.RequestHandlerMiddleware

object TokenAuthMiddleware {

  val tokenAuthMiddleware
      : RequestHandlerMiddleware[Nothing, UserTokenStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header("X-API-Key").map(_.value.toString)
      UserTokenStore.isValidToken(maybeHeader.getOrElse(""))
    })
}
