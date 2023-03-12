package ie.deed.ber.api

import zio.*
import zio.http.*
import zio.http.model.Method

object TokenGenerationApp {

  def apply(): Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> _ / "token" => Response.text("Token generated")
  } // @@ Middleware.csrfGenerate
}
