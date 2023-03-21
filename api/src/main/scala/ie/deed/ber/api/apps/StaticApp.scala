package ie.deed.ber.api.apps

import zio.http._
import zio.http.model.{Method, Status}

object StaticApp {

  val http: Http[Any, Throwable, Request, Response] =
    Http.collectHandler[Request] { case Method.GET -> "" /: "static" /: path =>
      Http.fromResource(path.encode).toHandler(Handler.notFound)
    }
}
