package ie.deed.ber.api

import ie.deed.ber.api.dao.UserDao
import pdi.jwt.JwtClaim
import zhttp.http.*

object JwtApp extends App {

  def http(claim: JwtClaim): Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "user" / "info" =>
      Response.json(claim.toJson)
    case Method.GET -> !! / "user" / "expiration" =>
      Response.text(s"Expires in: ${claim.expiration.getOrElse(-1L)}")
  }
}
