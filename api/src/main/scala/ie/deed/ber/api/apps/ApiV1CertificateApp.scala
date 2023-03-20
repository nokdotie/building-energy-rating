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
      certificateNumber: Int,
      certificateIssuedOn: String,
      certificateValidUntil: String,
      propertyAddress: String,
      propertyConstructedOn: Int,
      propertyType: String,
      floorAreaInSquareMeter: Float,
      energyRatingInKilowattHourPerSquareMetrePerYear: Float,
      carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear: Float
  )

  object Certificate {
    def fromInternal(internal: InternalCertificate): Certificate =
      Certificate(
        certificateNumber = internal.number.value,
        certificateIssuedOn = internal.seaiIeHtmlCertificate
          .fold("")(_.issuedOn.toString),
        certificateValidUntil = internal.seaiIeHtmlCertificate
          .fold("")(_.validUntil.toString),
        propertyAddress = internal.seaiIeHtmlCertificate.fold(
          ""
        )(_.propertyAddress.value),
        propertyConstructedOn = internal.seaiIeHtmlCertificate
          .fold(0)(_.propertyConstructedOn.getValue),
        propertyType = internal.seaiIeHtmlCertificate.fold("")(
          _.propertyType.toString
        ),
        floorAreaInSquareMeter = internal.seaiIeHtmlCertificate
          .fold(0f)(_.propertyFloorArea.value),
        energyRatingInKilowattHourPerSquareMetrePerYear =
          internal.seaiIeHtmlCertificate.fold(0f)(
            _.energyRating.value
          ),
        carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
          internal.seaiIeHtmlCertificate.fold(0f)(
            _.carbonDioxideEmissionsIndicator.value
          )
      )

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
