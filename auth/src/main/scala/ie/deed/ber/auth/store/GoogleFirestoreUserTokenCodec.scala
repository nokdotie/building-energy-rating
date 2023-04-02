package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{ApiKeyType, UserApiKey}
import ie.deed.ber.common.utils.MapUtils.getNested

import java.time.Instant
import scala.util.Try

object GoogleFirestoreUserTokenCodec {

  def decode(
      token: String,
      map: java.util.Map[String, Any]
  ): UserApiKey = {
    UserApiKey(
      email = map
        .getNested[String]("email")
        .getOrElse(""),
      apiKey = token,
      tokenType = map
        .getNested[String]("tokenType")
        .flatMap(s => Try { ApiKeyType.valueOf(s) })
        .getOrElse(ApiKeyType.Dev),
      createdAt = map
        .getNested[Timestamp]("createdAt")
        .map { _.toDate.toInstant }
        .getOrElse(Instant.EPOCH)
    )
  }
}
