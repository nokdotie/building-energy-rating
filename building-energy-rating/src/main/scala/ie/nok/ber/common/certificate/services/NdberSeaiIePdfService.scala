package ie.nok.ber.common.certificate.services

import ie.nok.ber.common.certificate.{Certificate, CertificateNumber}
import ie.nok.ber.common.utils.ZioHttpResponseUtils.responseToFile
import ie.nok.ber.common.certificate.utils.PdfParser
import java.io.File
import scala.util.Failure
import zio.{durationInt, ZIO}
import zio.Schedule.{recurs, fixed}
import zio.http.Client
import zio.http.model.HeaderValues.applicationOctetStream

object NdberSeaiIePdfService {
  def url(certificateNumber: CertificateNumber): String =
    s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"

  def getFile(
      certificateNumber: CertificateNumber
  ): ZIO[Client, Throwable, Option[File]] =
    Client
      .request(url(certificateNumber))
      .retry(recurs(3) && fixed(1.second))
      .flatMap {
        case response if response.hasContentType(applicationOctetStream) =>
          responseToFile(response).asSome
        case _ => ZIO.none
      }

  def getCertificate(
      certificateNumber: CertificateNumber
  ): ZIO[Client, Throwable, Option[Certificate]] =
    for {
      file <- getFile(certificateNumber)
      certificate <- file.fold(ZIO.none) { file =>
        ZIO.fromTry {
          PdfParser
            .tryParse(file)
            .recoverWith { throwable =>
              println(
                s"Failed to parse ${certificateNumber}: ${throwable.getMessage}"
              )
              throwable.printStackTrace()

              Failure(throwable)
            }
        }.asSome
      }
      _ <- file.fold(ZIO.unit) { file =>
        ZIO.attempt { file.delete() }
      }
    } yield certificate
}
