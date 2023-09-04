package ie.nok.ber

import scala.util.chaining.scalaUtilChainingOps
import zio.Random
import zio.json.{JsonCodec, DeriveJsonCodec}
import zio.stream.ZStream

case class CertificateNumber(value: Int)

object CertificateNumber {
  val MinValue: CertificateNumber = CertificateNumber(100_000_000)
  val MaxValue: CertificateNumber = CertificateNumber(110_000_000)

  def streamAllFrom(start: Int): ZStream[Any, Throwable, CertificateNumber] = {
    val startOrMin = Math.max(start, MinValue.value)
    val startOrMax = Math.min(start, MaxValue.value)

    val startToMax = ZStream.range(startOrMin, MaxValue.value)
    val minToStart = ZStream.range(MinValue.value, startOrMax)

    startToMax
      .concat(minToStart)
      .map(CertificateNumber.apply)
  }

  val streamAllWithRandomStart: ZStream[Any, Throwable, CertificateNumber] =
    Random
      .nextIntBetween(MinValue.value, MaxValue.value)
      .pipe(ZStream.fromZIO)
      .flatMap(streamAllFrom)

  given JsonCodec[CertificateNumber] = DeriveJsonCodec.gen[CertificateNumber]
}
