package ie.deed.ber.common.certificate.services

import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import ie.deed.ber.common.utils.ZioHttpResponseUtils.responseToFile
import ie.deed.ber.common.certificate.utils.PdfParser
import java.io.File
import zio.ZIO
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
      .retryN(3)
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
        ZIO.fromTry { PdfParser.tryParse(file) }.asSome
      }
    } yield certificate
}
