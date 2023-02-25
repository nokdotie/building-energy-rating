package ie.deed.ber.api

import ie.deed.ber.common.dao.BERRecordInMemoryDao
import zio.*
import zhttp.http.*
import zhttp.http.middleware.Cors.CorsConfig
import zhttp.service.Server

object MainApp extends ZIOAppDefault {

  // CORS
  private val corsConfig: CorsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  private val port = 8091
  private val routes = TokenGenerationApp.apply() ++ new BERApp(BERRecordInMemoryDao).http
    
  private val routesWithMiddleware =
    routes @@ Middleware.debug @@ Middleware.cors(corsConfig) @@ Middleware
      .csrfGenerate()

  private val httpServer = for {
    _ <- Console.printLine(s"Starting server on http://localhost:$port")
    _ <- Server.start(port, routesWithMiddleware)
  } yield ExitCode.success

  override def run: ZIO[Any, Any, Any] = httpServer
}
