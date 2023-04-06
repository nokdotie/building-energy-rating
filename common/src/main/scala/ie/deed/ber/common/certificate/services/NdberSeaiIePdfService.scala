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
  ): ZIO[Client, Throwable, File] =
    Client
      .request(url(certificateNumber))
      .filterOrFail { _.hasContentType(applicationOctetStream) } {
        new Throwable(s"Invalid content type")
      }
      .retryN(3)
      .flatMap { responseToFile }

  def getCertificate(
      certificateNumber: CertificateNumber
  ): ZIO[Client, Throwable, Certificate] =
    for {
      file <- getFile(certificateNumber)
      certificate <- ZIO.fromTry { PdfParser.tryParse(file) }
    } yield certificate
}
