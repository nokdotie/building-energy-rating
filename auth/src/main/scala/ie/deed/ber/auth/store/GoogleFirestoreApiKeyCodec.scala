package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{ApiKey, ApiKeyType}
import ie.deed.ber.common.utils.MapUtils.getNested

import scala.jdk.CollectionConverters.MapHasAsJava
import java.time.Instant
import java.util.Date
import scala.util.Try

object GoogleFirestoreApiKeyCodec {

  def decode(
      apiKey: String,
      map: java.util.Map[String, Any]
  ): ApiKey = {
    ApiKey(
      email = map
        .getNested[String]("email")
        .getOrElse(""),
      apiKey = apiKey,
      apiKeyType = map
        .getNested[String]("apiKeyType")
        .flatMap(s => Try { ApiKeyType.valueOf(s) })
        .getOrElse(ApiKeyType.Dev),
      createdAt = map
        .getNested[Timestamp]("createdAt")
        .map { t => Instant.ofEpochSecond(t.getSeconds, t.getNanos.toLong) }
        .getOrElse(Instant.EPOCH)
    )
  }

  def encode(apiKey: ApiKey): java.util.Map[String, Any] =
    Map(
      "apiKeyType" -> apiKey.apiKeyType.toString,
      "createdAt" -> Timestamp.ofTimeSecondsAndNanos(
        apiKey.createdAt.getEpochSecond,
        apiKey.createdAt.getNano
      ),
      "email" -> apiKey.email
    ).asJava
}
