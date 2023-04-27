package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.UserRequest

import scala.jdk.CollectionConverters.MapHasAsJava
import ie.deed.ber.common.utils.MapUtils.getNested

import java.time.Instant

object GoogleFirestoreUserRequestCodec {

  def encode(userRequest: UserRequest): java.util.Map[String, Any] = {
    Map(
      "email" -> userRequest.email,
      "timestamp" -> Timestamp.ofTimeSecondsAndNanos(
        userRequest.timestamp.getEpochSecond,
        userRequest.timestamp.getNano
      ),
      "request" -> userRequest.request
    ).asJava
  }

  def decode(map: java.util.Map[String, Any]): UserRequest = {
    UserRequest(
      email = map
        .getNested[String]("email")
        .getOrElse(""),
      timestamp = map
        .getNested[Timestamp]("timestamp")
        .map { t => Instant.ofEpochSecond(t.getSeconds, t.getNanos.toLong) }
        .getOrElse(Instant.EPOCH),
      request = map
        .getNested[String]("request")
        .getOrElse("")
    )
  }
}
