package ie.deed.ber.auth.token

import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.RequestHandlerMiddleware
import ie.deed.ber.auth.token.UserTokenStore

object TokenAuthMiddleware {

  val tokenAuthMiddleware
      : RequestHandlerMiddleware[Nothing, UserTokenStore, Throwable, Any] =
    customAuthZIO(headers => {
      val maybeHeader = headers.header("X-API-Key").map(_.value.toString)
      UserTokenStore.isValidToken(maybeHeader.getOrElse(""))
    })
}
