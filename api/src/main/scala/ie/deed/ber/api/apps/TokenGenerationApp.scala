package ie.deed.ber.api.apps

import ie.deed.ber.api.dao.{UserDao, UserInfo}
import ie.deed.ber.api.jwt.JwtHelper.jwtEncode
import io.grpc.xds.shaded.io.envoyproxy.envoy.config.trace.v2.Tracing
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.*
import zio.http.*
import zio.http.model.{HttpError, Method}

import java.time.Clock
import java.util.UUID

class TokenGenerationApp(userDao: UserDao) {

  val token: HttpApp[Any, Nothing] =
    Http.collect[Request] { case Method.GET -> !! / "token" / uuid(userId) =>
      userDao
        .getUser(userId)
        .fold(
          Response.fromHttpError(HttpError.Unauthorized("Invalid userId"))
        )(userInfo => Response.text(jwtEncode(userInfo)))
    }
}
