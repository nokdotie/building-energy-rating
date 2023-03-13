package ie.deed.ber.api.apps

import ie.deed.ber.common.certificate.{CertificateNumber, CertificateStore}
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO
import zio.http._
import zio.http.model.{Method, Status}
import zio.json._

object CertificateApp {

  val http: Http[CertificateStore, Nothing, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "ber" / int(certificateNumber) =>
        CertificateNumber(certificateNumber)
          .pipe { CertificateStore.getById }
          .fold(
            { case _ => Response(Status.InternalServerError) },
            {
              case None              => Response(Status.NotFound)
              case Some(certificate) => Response.json(certificate.toJson)
            }
          )
    }
}
