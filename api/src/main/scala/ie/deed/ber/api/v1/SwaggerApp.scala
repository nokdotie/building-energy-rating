package ie.deed.ber.api.v1

import zio.http._
import zio.http.model.{Method, Status}

object SwaggerApp {

  val http: Http[Any, Throwable, Request, Response] =
    Http.collectHandler[Request] {
      case Method.GET -> !! / "v1" / "swagger.yaml" =>
        Http.fromResource("swagger/v1.yaml").toHandler(Handler.notFound)
    }

}
