package ie.deed.ber.api

import ie.deed.ber.common.certificate.GoogleFirestoreCertificateStore
import zio._
import zio.http._
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig
import zio.gcp.firestore.Firestore

object MainApp extends ZIOAppDefault {

  // CORS
  private val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  private val routes =
    apps.CertificateApp.http ++
      TokenGenerationApp.apply()

  private val app =
    routes // @@ Middleware.debug @@ Middleware.cors(corsConfig) @@ Middleware.csrfGenerate()

  override val run = for {
    _ <- Console.printLine(s"Starting server on http://localhost:8080")
    _ <- Server
      .serve(app)
      .provide(
        Server.default,
        Firestore.live,
        GoogleFirestoreCertificateStore.layer,
        Scope.default
      )
  } yield ()

}
