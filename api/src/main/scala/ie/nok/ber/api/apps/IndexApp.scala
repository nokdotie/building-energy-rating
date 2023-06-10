package ie.nok.ber.api.apps

import zio.http._
import zio.http.model.{Method, Status}

object IndexApp {

  val http: Http[Any, Throwable, Request, Response] =
    Http.collectHandler[Request] { case Method.GET -> !! =>
      Http.fromResource("index.html").toHandler(Handler.notFound)
    }
}
