package ie.deed.ber.api.apps

import ie.deed.ber.common.certificate.{
  Certificate => InternalCertificate,
  CertificateNumber,
  CertificateStore
}
import ie.seai.ber.certificate.{HtmlCertificate, PdfCertificate}
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.json._

object ApiV1CertificateApp {

  case class Certificate(
      number: Int,
      rating: String,
      ratingImageUrl: String,
      issuedOn: String,
      validUntil: String,
      address: String,
      energyRatingInKilowattHourPerSquareMetrePerYear: Float,
      carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear: Float
  )

  object Certificate {
    def fromInternal(internal: InternalCertificate): Option[Certificate] =
      internal.seaiIeHtmlCertificate
        .orElse(internal.seaiIePdfCertificate)
        .collect {
          case certificate: HtmlCertificate =>
            (
              certificate.rating,
              certificate.issuedOn,
              certificate.validUntil,
              certificate.propertyAddress,
              certificate.energyRating,
              certificate.carbonDioxideEmissionsIndicator
            )
          case certificate: PdfCertificate =>
            (
              certificate.rating,
              certificate.issuedOn,
              certificate.validUntil,
              certificate.propertyAddress,
              certificate.energyRating,
              certificate.carbonDioxideEmissionsIndicator
            )
        }
        .map {
          (
              rating,
              issuedOn,
              validUntil,
              propertyAddress,
              energyRating,
              carbonDioxideEmissionsIndicator
          ) =>
            Certificate(
              number = internal.number.value,
              rating = rating.toString,
              ratingImageUrl =
                s"https://ber.deed.ie/static/images/ber/$rating.svg",
              issuedOn = issuedOn.toString,
              validUntil = validUntil.toString,
              address = propertyAddress.value,
              energyRatingInKilowattHourPerSquareMetrePerYear =
                energyRating.value,
              carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
                carbonDioxideEmissionsIndicator.value
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
          .some
          .fold(
            {
              case None => Response(Status.NotFound)
              case _    => Response(Status.InternalServerError)
            },
            certificate => Response.json(certificate.toJson)
          )
    }
}
