package ie.deed.ber.common.certificate

final case class CertificateNumber(value: Int) extends AnyVal

object CertificateNumber {
  val MinValue: CertificateNumber = CertificateNumber(100_000_000)
  val MaxValue: CertificateNumber = CertificateNumber(110_000_000)
}
