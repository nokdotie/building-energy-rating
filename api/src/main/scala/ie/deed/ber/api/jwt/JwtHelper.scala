package ie.deed.ber.api.jwt

import ie.deed.ber.api.dao.UserInfo
import ie.deed.ber.api.dao.UserInfo.codecUserInfo
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.util.AsciiString
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.json.EncoderOps

import java.time.Clock

object JwtHelper {

  implicit val clock: Clock = Clock.systemUTC

  // Secret Authentication key
  private val SECRET_KEY = "secret"

  private val JWT_ALGORITHM = JwtAlgorithm.HS512

  val AUTH_HEADER: AsciiString = HttpHeaderNames.WWW_AUTHENTICATE

  // Helper to encode the JWT token
  def jwtEncode(userInfo: UserInfo): String = {
    val json = userInfo.toJson
    val claim = JwtClaim {
      json
    }.issuedNow.expiresIn(5 * 60)
    Jwt.encode(claim, SECRET_KEY, JWT_ALGORITHM)
  }

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] = {
    Jwt.decode(token, SECRET_KEY, Seq(JWT_ALGORITHM)).toOption
  }
}
