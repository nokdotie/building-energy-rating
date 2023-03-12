package ie.deed.ber.common.certificate

import java.time.{LocalDate, Year}
import scala.util.{Failure, Success}
import zio.json._

import ie.deed.ber.common.certificate.seaiie.{Certificate => SeaiIeCertificate}

final case class Certificate(
    number: CertificateNumber,
    `seai.ie`: Option[SeaiIeCertificate]
)

object Certificate {
  implicit val encoder: JsonEncoder[Certificate] =
    DeriveJsonEncoder.gen[Certificate]
}
