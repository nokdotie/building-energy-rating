package ie.deed.ber.api

import ie.deed.ber.api.dao.*
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.http.Server
import zio.*
import zhttp.http.*
import zio.json.*

import java.time.Clock
import java.util.UUID

class AuthenticationApp(userDao: UserDao) extends App {
  // Secret Authentication key
  private val SECRET_KEY = "secret"

  implicit val clock: Clock = Clock.systemUTC

  private val jwtAlgorithm = JwtAlgorithm.HS512

  // Helper to encode the JWT token
  private def jwtEncode(userInfo: UserInfo): String = {
    val json = userInfo.toJson
    val claim = JwtClaim { json }.issuedNow.expiresIn(5 * 60)
    Jwt.encode(claim, SECRET_KEY, jwtAlgorithm)
  }

  // Helper to decode the JWT token
  private def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(jwtAlgorithm)).toOption
  }

  // Authentication middleware
  // Takes in a Failing HttpApp and a Succeed HttpApp which are called based on Authentication success or failure
  // For each request tries to read the `X-ACCESS-TOKEN` header
  // Validates JWT Claim
  def authenticate[R, E](
      fail: HttpApp[R, E],
      success: JwtClaim => HttpApp[R, E]
  ): HttpApp[R, E] =
    Http
      .fromFunction[Request] {
        _.header("X-ACCESS-TOKEN")
          .flatMap(header => jwtDecode(header._2.toString))
          .fold[HttpApp[R, E]](fail)(success)
      }
      .flatten

  val token: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "token" / zhttp.http.uuid(userId) =>
      userDao
        .getUser(userId)
        .fold(
          Response.fromHttpError(HttpError.Unauthorized("Invalid userId"))
        )(userInfo => Response.text(jwtEncode(userInfo)))
  }
}
