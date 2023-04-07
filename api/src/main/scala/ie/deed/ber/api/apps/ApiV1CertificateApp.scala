package ie.deed.ber.api.apps

import ie.deed.ber.api.model.Certificate
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  Certificate as InternalCertificate
}
import ie.deed.ber.common.certificate.services.NdberSeaiIePdfService
import ie.deed.ber.common.certificate.stores.CertificateStore
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http.*
import zio.http.model.{Method, Status}
import zio.json.EncoderOps

object ApiV1CertificateApp {

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
      case Method.GET -> !! / "api" / "v1" / "ber" / int(certificateNumber) =>
        CertificateNumber(certificateNumber)
          .pipe { getCertificateFromStoreOrService }
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
