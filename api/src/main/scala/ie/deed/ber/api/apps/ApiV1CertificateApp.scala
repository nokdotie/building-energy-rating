package ie.deed.ber.api.apps

import ie.deed.ber.api.model.Certificate
import ie.deed.ber.api.model.Certificate.encoder
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  Certificate as InternalCertificate
}
import ie.deed.ber.common.certificate.stores.CertificateStore
import ie.seai.ber.certificate.{HtmlCertificate, PdfCertificate}

import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http.*
import zio.http.model.{Method, Status}
import zio.json.*

object ApiV1CertificateApp {

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
