package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{TokenType, UserToken}
import ie.deed.ber.common.utils.MapUtils.getTyped

import java.time.Instant
import scala.util.Try

object GoogleFirestoreUserTokenCodec {

  def decode(
      token: String,
      map: java.util.Map[String, Any]
  ): UserToken = {
    UserToken(
      email = map
        .getTyped[String]("email")
        .getOrElse(""),
      token = token,
      tokenType = map
        .getTyped[String]("tokenType")
        .flatMap(s => Try { TokenType.valueOf(s) })
        .getOrElse(TokenType.Dev),
      createdAt = map
        .getTyped[Timestamp]("createdAt")
        .map { _.toDate.toInstant }
        .getOrElse(Instant.EPOCH)
    )
  }
}
