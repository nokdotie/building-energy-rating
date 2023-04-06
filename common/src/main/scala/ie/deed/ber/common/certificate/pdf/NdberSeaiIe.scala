package ie.deed.ber.common.certificate.pdf

import java.time.{LocalDate, Year}
import ie.deed.ber.common.certificate.CertificateNumber

object NdberSeaiIe {
  def url(certificateNumber: CertificateNumber): String =
    s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"
}
