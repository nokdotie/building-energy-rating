package ie.deed.ber.auth.middleware

import ie.deed.ber.auth.model.{ApiKey, ApiKeyType, UserRequest}
import ie.deed.ber.auth.store.{ApiKeyStore, CreditStore, UserRequestStore}
import zio.{Trace, ZIO}
import zio.http.HttpAppMiddleware.customAuthZIO
import zio.http.middleware.RequestHandlerMiddlewares.interceptPatchZIO
import zio.http.model.Status
import zio.http.{Handler, Patch, Request, RequestHandlerMiddleware, Response}

import java.time.Instant

object UserRequestStoreAuthMiddleware {

  val userRequestStoreAuthMiddleware: RequestHandlerMiddleware[
    Nothing,
    ApiKeyStore with UserRequestStore with CreditStore,
    Throwable,
    Any
  ] =
    new RequestHandlerMiddleware.Simple[
      ApiKeyStore with UserRequestStore with CreditStore,
      Throwable
    ] {
      override def apply[
          Env <: ApiKeyStore with UserRequestStore with CreditStore,
          Err >: Throwable
      ](
          handler: Handler[Env, Err, Request, Response]
      )(implicit trace: Trace): Handler[Env, Err, Request, Response] =
        Handler
          .fromFunctionZIO[Request] { request =>
            val maybeHeader =
              request.headers.header(Headers.ApiKey).map(_.value.toString)
            ApiKeyStore.getApiKey(maybeHeader.getOrElse("")).flatMap {
              // we reject requests without ApiKey
              case None => ZIO.succeed(Handler.status(Status.Unauthorized))
              // we save requests only for User type (not Admin nor Dev)
              case Some(ApiKey(email, _, ApiKeyType.User, _)) =>
                UserRequestStore
                  .saveUserRequest(
                    UserRequest(email, Instant.now(), request.toString)
                  )
                  .flatMap { _ =>
                    // UserRequest was successfully saved
                    for {
                      numberOfRequests <- UserRequestStore.countUserRequests(
                        email
                      ) // check how many requests user did
                      numberOfCredits <- CreditStore.getNumberOfCredits(
                        email
                      ) // check number of available credits for user by email
                    } yield {
                      if (numberOfRequests <= numberOfCredits) handler
                      else Handler.status(Status.PaymentRequired)
                    }
                  }
              // we pass the request if the ApiKey was valid
              case _ => ZIO.succeed(handler)
            }
          }
          .flatten
    }
}
