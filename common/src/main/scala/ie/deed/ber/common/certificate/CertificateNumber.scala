package ie.deed.ber.common.certificate

import scala.util.chaining.scalaUtilChainingOps
import zio.Random
import zio.stream.ZStream

final case class CertificateNumber(value: Int) extends AnyVal

object CertificateNumber {
  val MinValue: CertificateNumber = CertificateNumber(100_000_000)
  val MaxValue: CertificateNumber = CertificateNumber(110_000_000)

  val streamAllWithRandomStart: ZStream[Any, Throwable, CertificateNumber] =
    Random
      .nextIntBetween(MinValue.value, MaxValue.value)
      .pipe(ZStream.fromZIO)
      .flatMap { mid =>
        val midEnd = ZStream.range(mid, MaxValue.value)
        val startMid = ZStream.range(MinValue.value, mid)

        midEnd.concat(startMid)
      }
      .map(CertificateNumber.apply)
}
