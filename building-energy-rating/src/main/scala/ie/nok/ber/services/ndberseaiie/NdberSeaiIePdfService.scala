package ie.nok.ber.services.ndberseaiie

import ie.nok.ber.{Certificate, CertificateNumber}
import ie.nok.http.Client.requestBodyAsTempFile
import zio.Schedule.{fixed, recurs}
import zio.http.Client
import zio.http.model.HeaderValues.applicationOctetStream
import zio.{Scope, ZIO, durationInt}

import java.io.File
import scala.util.Failure
import scala.util.chaining.scalaUtilChainingOps

object NdberSeaiIePdfService {
  def getUrl(certificateNumber: CertificateNumber): String =
    s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"

  private val notFoundResponseBodyLength =
    "File Not found or other exception handled by system. Please consult your system administrator.".length

  private def getFile(
      url: String
  ): ZIO[Client & Scope, Throwable, Option[File]] =
    requestBodyAsTempFile(url)
      .retry(recurs(3) && fixed(1.second))
      .map { _.toFile }
      .asSome
      .map { _.filter { _.length > notFoundResponseBodyLength } }

  def getCertificate(
      certificateNumber: CertificateNumber
  ): ZIO[Client & Scope, Throwable, Option[Certificate]] = {
    val url = getUrl(certificateNumber)
    for {
      file <- getFile(url)
      certificate <- file.fold(ZIO.none) { file =>
        ZIO.fromTry {
          NdberSeaiIePdfParser
            .tryParse(url, file)
            .recoverWith { throwable =>
              println(
                s"Failed to parse $certificateNumber: ${throwable.getMessage}"
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
}
