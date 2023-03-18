package ie.deed.ber.api.apps

import ie.deed.ber.api.dao.UserDao
import ie.deed.ber.api.jwt.JwtHelper
import ie.deed.ber.api.jwt.JwtHelper.jwtDecode
import pdi.jwt.JwtClaim
import zio.http.HttpAppMiddleware.bearerAuth
import zio.http.model.{HttpError, Method}
import zio.http.*

object UserInfoApp {

  val http: HttpApp[Any, Nothing] =
    Http.collect[Request] { case req @ Method.GET -> !! / "user" / "info" =>
      val maybeJwtClaim = req
        .header(JwtHelper.AUTH_HEADER)
        .flatMap(header => JwtHelper.jwtDecode(header.value.toString))
      maybeJwtClaim.fold(
        Response.fromHttpError(HttpError.Unauthorized("Invalid user"))
      ) { claim =>
        Response.json(claim.toJson)
      }
    } @@ bearerAuth(jwtDecode(_).isDefined)
}
