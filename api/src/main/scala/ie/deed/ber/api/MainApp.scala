package ie.deed.ber.api

import ie.deed.ber.api.token.{TokenAuthMiddleware, UserTokenInMemoryStore}
import ie.deed.ber.api.apps.{ApiV1CertificateApp, StaticApp, HealthApp}
import ie.deed.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import zio.*
import zio.http.*
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig
import zio.gcp.firestore.Firestore

object MainApp extends ZIOAppDefault {

  private val port = 8080

  // CORS
  private val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  private val routes =
    ApiV1CertificateApp.http @@ TokenAuthMiddleware.tokenAuthMiddleware ++
      StaticApp.http ++
      HealthApp.http

  private val app = (routes
    @@ HttpAppMiddleware.debug
    @@ HttpAppMiddleware.cors(corsConfig)).withDefaultErrorResponse

  override val run: ZIO[Any, Throwable, Unit] = for {
    _ <- Console.printLine(s"Starting server on http://localhost:$port")
    _ <- Server
      .serve(app)
      .provide(
        Server.defaultWithPort(port),
        Firestore.live,
        GoogleFirestoreCertificateStore.layer,
        Scope.default,
        UserTokenInMemoryStore.layer
      )
  } yield ()
}
