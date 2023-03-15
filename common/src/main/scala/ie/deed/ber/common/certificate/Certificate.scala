package ie.deed.ber.common.certificate

import java.time.{LocalDate, Year}
import scala.util.{Failure, Success}

final case class Certificate(
    number: CertificateNumber,
    `ndber.seai.ie/pass/ber/search.aspx`: Option[
      ndberseaiiepassbersearchaspx.Certificate
    ],
    `ndber.seai.ie/pass/download/passdownloadber.ashx`: Option[
      ndberseaiiepassdownloadpassdownloadberashx.Certificate
    ]
)
