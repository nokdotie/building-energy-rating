package ie.deed.ber.api

import ie.deed.ber.auth.middleware.ApiKeyAuthMiddleware
import ie.deed.ber.api.apps._
import ie.deed.ber.auth.store.{GoogleFirestoreApiKeyStore, ApiKeyInMemoryStore}
import ie.deed.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import zio.*
import zio.http.*
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig
import zio.gcp.firestore.Firestore

object MainApp extends ZIOAppDefault {

  private val port = 8080

  private val routes =
    v1.apps ++
      IndexApp.http ++
      StaticApp.http ++
      HealthApp.http

  private val app = (
    routes
      @@ HttpAppMiddleware.debug
      @@ HttpAppMiddleware.cors()
  ).withDefaultErrorResponse

  override val run: ZIO[Any, Throwable, Unit] = for {
    _ <- Console.printLine(s"Starting server on http://localhost:$port")
    _ <- Server
      .serve(app)
      .provide(
        Server.defaultWithPort(port),
        Firestore.live,
        GoogleFirestoreCertificateStore.layer,
        Scope.default,
        Client.default,
        GoogleFirestoreApiKeyStore.layer // use UserApiKeyInMemoryStore.layer for local development
      )
  } yield ()
}
