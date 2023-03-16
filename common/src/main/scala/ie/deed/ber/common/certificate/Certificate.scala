package ie.deed.ber.common.certificate

import java.time.{LocalDate, Year}
import scala.util.{Failure, Success}
import ie.seai.ber.certificate.{HtmlCertificate, PdfCertificate}

case class Certificate(
    number: CertificateNumber,
    seaiIeHtmlCertificate: Option[HtmlCertificate],
    seaiIePdfCertificate: Option[PdfCertificate]
)
