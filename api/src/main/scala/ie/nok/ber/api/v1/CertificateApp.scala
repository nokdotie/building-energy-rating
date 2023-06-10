package ie.nok.ber.api.v1

import ie.nok.ber.common.certificate.{
  CertificateNumber,
  Certificate as InternalCertificate,
  Eircode
}
import ie.nok.ber.common.certificate.services.NdberSeaiIePdfService
import ie.nok.ber.common.certificate.stores.CertificateStore
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http.*
import zio.http.model.{Method, Status}
import zio.json.{DeriveJsonEncoder, JsonEncoder, EncoderOps}

object CertificateApp {

  case class Certificate(
      number: Int,
      rating: String,
      ratingImageUrl: String,
      issuedOn: String,
      validUntil: String,
      property: Property,
      assessor: Assessor,
      domesticEnergyAssessmentProcedureVersion: String,
      energyRating: Float,
      carbonDioxideEmissionsIndicator: Float
  )
  given JsonEncoder[Certificate] = DeriveJsonEncoder.gen[Certificate]

  case class Property(
      address: String,
      eircode: Option[String]
  )
  given JsonEncoder[Property] = DeriveJsonEncoder.gen[Property]

  case class Assessor(
      number: Int,
      companyNumber: Int
  )
  given JsonEncoder[Assessor] = DeriveJsonEncoder.gen[Assessor]

  def fromInternal(internal: InternalCertificate): Certificate =
    Certificate(
      number = internal.number.value,
      rating = internal.rating.toString,
      ratingImageUrl =
        s"https://ber.nok.ie/static/images/ber/${internal.rating}.svg",
      issuedOn = internal.issuedOn.toString,
      validUntil = internal.validUntil.toString,
      property = Property(
        address = internal.propertyAddress.value,
        eircode = internal.propertyEircode.map(_.value)
      ),
      assessor = Assessor(
        number = internal.assessorNumber.value,
        companyNumber = internal.assessorCompanyNumber.value
      ),
      domesticEnergyAssessmentProcedureVersion =
        internal.domesticEnergyAssessmentProcedureVersion.toString,
      energyRating = internal.energyRating.value,
      carbonDioxideEmissionsIndicator =
        internal.carbonDioxideEmissionsIndicator.value
    )

  def getCertificateFromStoreOrService(
      certificateNumber: CertificateNumber
  ): ZIO[CertificateStore with Client, Throwable, Option[InternalCertificate]] =
    CertificateStore
      .getByNumber(certificateNumber)
      .filterOrElse(_.isDefined) {
        NdberSeaiIePdfService
          .getCertificate(certificateNumber)
          .tapSome { case Some(certificate) =>
            CertificateStore.upsertBatch(List(certificate))
          }
      }

  val http: Http[CertificateStore with Client, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "v1" / "ber" / int(certificateNumber) =>
        CertificateNumber(certificateNumber)
          .pipe { getCertificateFromStoreOrService }
          .some
          .map { fromInternal }
          .fold(
            {
              case None => Response(Status.NotFound)
              case _    => Response(Status.InternalServerError)
            },
            certificate => Response.json(certificate.toJson)
          )

      case Method.GET -> !! / "v1" / "eircode" / eircode / "ber" =>
        Eircode(eircode)
          .pipe { CertificateStore.getAllByEircode }
          .map { _.map { fromInternal } }
          .fold(
            _ => Response(Status.InternalServerError),
            {
              case Nil         => Response(Status.NotFound)
              case certificate => Response.json(certificate.toJson)
            }
          )
    }
}
