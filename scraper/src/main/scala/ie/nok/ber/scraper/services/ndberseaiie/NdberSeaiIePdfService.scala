package ie.nok.ber.scraper.services.ndberseaiie

import ie.nok.ber.common.certificate.{Certificate, CertificateNumber}
import ie.nok.http.Client.requestBodyAsTempFile
import java.io.File
import scala.util.Failure
import scala.util.chaining.scalaUtilChainingOps
import zio.{durationInt, Scope, ZIO}
import zio.Schedule.{recurs, fixed}
import zio.http.Client
import zio.http.model.HeaderValues.applicationOctetStream

object NdberSeaiIePdfService {
  def url(certificateNumber: CertificateNumber): String =
    s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"

  val notFoundResponseBody =
    "File Not found or other exception handled by system. Please consult your system administrator."
  val notFoundResponseBodyLength = notFoundResponseBody.length()

  def getFile(
      certificateNumber: CertificateNumber
  ): ZIO[Client & Scope, Throwable, Option[File]] =
    url(certificateNumber)
      .pipe { requestBodyAsTempFile(_) }
      .retry(recurs(3) && fixed(1.second))
      .map { _.toFile }
      .asSome
      .map { _.filter { _.length > notFoundResponseBodyLength } }

  def getCertificate(
      certificateNumber: CertificateNumber
  ): ZIO[Client & Scope, Throwable, Option[Certificate]] =
    for {
      file <- getFile(certificateNumber)
      certificate <- file.fold(ZIO.none) { file =>
        ZIO.fromTry {
          NdberSeaiIePdfParser
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
