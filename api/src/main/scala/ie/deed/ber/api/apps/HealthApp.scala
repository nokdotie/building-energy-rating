package ie.deed.ber.api.apps

import zio.http._
import zio.http.model.{Method, Status}

object HealthApp {

  val http: HttpApp[Any, Nothing] =
    Http.collect[Request] {
      case Method.GET -> !! / "readyz" => Response(Status.Ok)
      case Method.GET -> !! / "livez"  => Response(Status.Ok)
    }
}
