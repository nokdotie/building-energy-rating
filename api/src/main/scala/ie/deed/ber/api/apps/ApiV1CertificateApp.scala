package ie.deed.ber.api.apps

import ie.deed.ber.common.certificate.{
  Certificate => InternalCertificate,
  CertificateNumber,
  CertificateStore
}
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.json._

object ApiV1CertificateApp {

  case class Certificate(
      number: Int,
      rating: String,
      issuedOn: String,
      validUntil: String,
      address: String,
      energyRatingInKilowattHourPerSquareMetrePerYear: Float,
      carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear: Float
  )

  object Certificate {
    def fromInternal(internal: InternalCertificate): Certificate = {
      val html = internal.seaiIeHtmlCertificate
      val pdf = internal.seaiIePdfCertificate

      Certificate(
        number = internal.number.value,
        rating = html
          .map(_.rating)
          .orElse(pdf.map(_.rating))
          .fold("") { _.toString },
        issuedOn = html
          .map(_.issuedOn)
          .orElse(pdf.map(_.issuedOn))
          .fold("") { _.toString },
        validUntil = html
          .map(_.validUntil)
          .orElse(pdf.map(_.validUntil))
          .fold("") { _.toString },
        address = html
          .map(_.propertyAddress)
          .orElse(pdf.map(_.propertyAddress))
          .fold("") { _.value },
        energyRatingInKilowattHourPerSquareMetrePerYear = html
          .map(_.energyRating)
          .orElse(pdf.map(_.energyRating))
          .fold(0f)(_.value),
        carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
          html
            .map(_.carbonDioxideEmissionsIndicator)
            .orElse(pdf.map(_.carbonDioxideEmissionsIndicator))
            .fold(0f)(_.value),
      )
    }

    implicit val encoder: JsonEncoder[Certificate] =
      DeriveJsonEncoder.gen[Certificate]
  }

  val http: Http[CertificateStore, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "api" / "v1" / "ber" / int(certificateNumber) =>
        CertificateNumber(certificateNumber)
          .pipe { CertificateStore.getById }
          .some
          .map { Certificate.fromInternal }
          .fold(
            {
              case None => Response(Status.NotFound)
              case _    => Response(Status.InternalServerError)
            },
            certificate => Response.json(certificate.toJson)
          )
    }
}
