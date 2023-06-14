package ie.nok.ber.api

import ie.nok.ber.api.apps.{CertificateApp, HealthApp}
import ie.nok.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import ie.nok.gcp.firestore.Firestore
import scala.language.postfixOps
import zio.{Console, Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, HttpAppMiddleware, Server}
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig

object MainApp extends ZIOAppDefault {

  private val port = 8080

  private val routes =
    CertificateApp.http ++
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
        Client.default
      )
  } yield ()
}
