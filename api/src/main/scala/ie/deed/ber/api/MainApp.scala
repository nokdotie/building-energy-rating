package ie.deed.ber.api

import ie.deed.ber.api.apps.*
import ie.deed.ber.api.dao.UserInMemoryDao
import ie.deed.ber.api.jwt.JwtHelper.jwtDecode
import ie.deed.ber.common.certificate.GoogleFirestoreCertificateStore
import zio.*
import zio.http.{HttpApp, *}
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig
import zio.gcp.firestore.Firestore
import zio.http.HttpAppMiddleware.bearerAuth

import java.net.http.HttpResponse

object MainApp extends ZIOAppDefault {

  // CORS
  private val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  private val routes = HealthApp.http
    ++ TokenGenerationApp(UserInMemoryDao).token
    ++ ApiV1CertificateApp.http
    ++ UserInfoApp.http

  private val app = (routes
    @@ HttpAppMiddleware.debug
    @@ HttpAppMiddleware.cors(corsConfig)).withDefaultErrorResponse

  override val run: ZIO[Any, Throwable, Unit] = for {
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
