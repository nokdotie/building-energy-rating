package ie.deed.ber.common.certificate

import zio.json._

final case class CertificateNumber(value: Int) extends AnyVal
object CertificateNumber {
  implicit val encoder: JsonEncoder[CertificateNumber] =
    JsonEncoder[Int].contramap(_.value)
}
