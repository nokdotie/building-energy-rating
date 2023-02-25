package ie.deed.ber.api

import zhttp.http.*
import zio.*

object TokenGenerationApp {

  def apply(): Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> _ / "token" => Response.text("Token generated")
  } @@ Middleware.csrfGenerate()
}
