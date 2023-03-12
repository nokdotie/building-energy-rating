package ie.deed.ber.api

import ie.deed.ber.common.dao.BERRecordInMemoryDao
import zio._
import zio.http._
import zio.http.model.Method
import zio.http.middleware.Cors.CorsConfig

object MainApp extends ZIOAppDefault {

  // CORS
  private val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  private val routes =
    TokenGenerationApp.apply() ++ BERApp(BERRecordInMemoryDao).http

  private val app =
    routes // @@ Middleware.debug @@ Middleware.cors(corsConfig) @@ Middleware.csrfGenerate()

  override val run = for {
    _ <- Console.printLine(s"Starting server on http://localhost:8080")
    _ <- Server.serve(app).provide(Server.default)
  } yield ()

}
