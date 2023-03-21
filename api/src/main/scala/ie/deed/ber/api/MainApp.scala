package ie.deed.ber.api

import ie.deed.ber.api.apps.{ApiV1CertificateApp, HealthApp}
import ie.deed.ber.common.certificate.GoogleFirestoreCertificateStore
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
    ApiV1CertificateApp.http ++
      HealthApp.http

  private val app = (routes
    @@ HttpAppMiddleware.debug
    @@ HttpAppMiddleware.cors(corsConfig)).withDefaultErrorResponse

  override val run = for {
    _ <- Console.printLine(s"Starting server on http://localhost:$port")
    _ <- Server
      .serve(app)
      .provide(
        Server.defaultWithPort(port),
        Firestore.live,
        GoogleFirestoreCertificateStore.layer,
        Scope.default
      )
  } yield ()
}
