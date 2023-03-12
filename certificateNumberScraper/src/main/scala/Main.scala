import zio.{durationInt, Random, Schedule, Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.gcp.firestore.Firestore
import zio.stream.{ZPipeline, ZStream}
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import scala.util.chaining.scalaUtilChainingOps
import ie.deed.ber.common.certificate.Certificate

val certificateNumbers: ZStream[Any, Throwable, CertificateNumber] = {
  val start = 100_000_000
  val end = 110_000_000

  Random
    .nextIntBetween(start, end)
    .pipe { ZStream.fromZIO }
    .flatMap { mid =>
      val midEnd = ZStream.range(mid, end)
      val startMid = ZStream.range(start, mid)

      midEnd.concat(startMid)
    }
    .map { CertificateNumber.apply }
}

val filterExists: ZPipeline[
  Client,
  Throwable,
  CertificateNumber,
  CertificateNumber
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { Option.when(_)(certificateNumber) }
    }
    .collectSome
}

val upsert: ZPipeline[CertificateStore, Throwable, CertificateNumber, Int] =
  ZPipeline
    .map { Certificate(_, None) }
    .groupedWithin(100, 10.seconds)
    .mapZIO { chunks => CertificateStore.upsertBatch(chunks.toList).retryN(3) }
    .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

def certificateUrl(certificateNumber: CertificateNumber): String =
  s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"

def resourceExists(url: String): ZIO[Client, Throwable, Boolean] = {
  val timeoutAfter = 2.seconds
  val retryAfter = 1.second

  Client
    .request(url)
    .timeoutFail(Throwable("Timeout"))(timeoutAfter)
    .retry(Schedule.fixed(retryAfter))
    .flatMap { _.body.asString }
    .map { !_.startsWith("File Not found") }
}

val upsertLimit = 1_000

val app: ZIO[Client with CertificateStore, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(filterExists)
    .debug("Certificate Number Exists")
    .via(upsert)
    .debug("Certificate Number Upserted")
    .takeWhile { _ < upsertLimit }
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    Client.fromConfig,
    ClientConfig.default,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Scope.default
  )
}
