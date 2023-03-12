package ie.deed.ber.common.certificate.seaiie

import zio.json._

final case class Address(value: String) extends AnyVal
object Address {
  implicit val encoder: JsonEncoder[Address] =
    JsonEncoder[String].contramap(_.value)
}
