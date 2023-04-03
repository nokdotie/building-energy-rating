import zio.{durationInt, Random, Schedule, Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.http.model.HeaderValues
import zio.gcp.firestore.Firestore
import zio.stream.{ZPipeline, ZStream}
import ie.deed.ber.common.certificate.CertificateNumber
import ie.deed.ber.common.certificate.stores.{
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import scala.util.chaining.scalaUtilChainingOps
import ie.deed.ber.common.certificate.Certificate
import ie.seai.ber.certificate.PdfCertificate

val certificateNumbers: ZStream[Any, Throwable, CertificateNumber] =
  Random
    .nextIntBetween(
      CertificateNumber.MinValue.value,
      CertificateNumber.MaxValue.value
    )
    .pipe { ZStream.fromZIO }
    .flatMap { mid =>
      val midEnd = ZStream.range(mid, CertificateNumber.MaxValue.value)
      val startMid = ZStream.range(CertificateNumber.MinValue.value, mid)

      midEnd.concat(startMid)
    }
    .map { CertificateNumber.apply }

val filterExists: ZPipeline[
  Client,
  Throwable,
  CertificateNumber,
  CertificateNumber
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = PdfCertificate.url(certificateNumber)

      resourceExists(url)
        .map { Option.when(_)(certificateNumber) }
    }
    .collectSome
}

def resourceExists(url: String): ZIO[Client, Throwable, Boolean] = {
  val timeoutAfter = 2.seconds
  val retryAfter = 1.second

  Client
    .request(url)
    .timeoutFail(Throwable("Timeout"))(timeoutAfter)
    .retry(Schedule.fixed(retryAfter))
    .map { _.hasContentType(HeaderValues.applicationOctetStream) }
}

val app: ZIO[Client with CertificateStore, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(filterExists)
    .debug("Certificate Number Exists")
    .map { Certificate(_, None, None, None) }
    .via(CertificateStore.upsertPipeline)
    .debug("Certificate Number Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Unit] = app.provide(
    Client.fromConfig,
    ClientConfig.default,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Scope.default
  )
}
