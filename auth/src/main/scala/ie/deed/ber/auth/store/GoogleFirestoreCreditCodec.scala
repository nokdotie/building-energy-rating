package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{Credit, UserRequest}
import ie.deed.ber.common.utils.MapUtils.getNested

import java.time.Instant
import scala.jdk.CollectionConverters.MapHasAsJava

object GoogleFirestoreCreditCodec {

  def encode(credit: Credit): java.util.Map[String, Any] = {
    Map(
      "email" -> credit.email,
      "timestamp" -> Timestamp.ofTimeSecondsAndNanos(
        credit.timestamp.getEpochSecond,
        credit.timestamp.getNano
      ),
      "number" -> credit.number
    ).asJava
  }

  def decode(map: java.util.Map[String, Any]): Credit = {
    Credit(
      email = map
        .getNested[String]("email")
        .getOrElse(""),
      timestamp = map
        .getNested[Timestamp]("timestamp")
        .map { t => Instant.ofEpochSecond(t.getSeconds, t.getNanos.toLong) }
        .getOrElse(Instant.EPOCH),
      number = map
        .getNested[Long]("number")
        .getOrElse(-1)
    )
  }
}
