import java.io.File
import java.nio.file.StandardOpenOption.APPEND
import zio.{durationInt, Schedule, Scope, ZIO, ZIOAppDefault, ZLayer}
import zio.http.{Client, ClientConfig, Middleware}
import zio.gcp.firestore.Firestore
import zio.stream.{ZPipeline, ZStream, ZSink}
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  CertificateNumberStore,
  GoogleFirestoreCertificateNumberStore
}
import scala.util.chaining.scalaUtilChainingOps

val certificateNumbers: ZStream[Any, Throwable, CertificateNumber] = {
  val smallestCertificateNumber = 100_000_000
  val biggestCertificateNumber = 110_000_000

  ZStream
    .range(smallestCertificateNumber, biggestCertificateNumber)
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

val upsert
    : ZPipeline[CertificateNumberStore, Throwable, CertificateNumber, Unit] =
  ZPipeline.chunks
    .mapZIO { chunks => CertificateNumberStore.upsertBatch(chunks.toList) }

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

val app: ZIO[Client with CertificateNumberStore, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(filterExists)
    .debug("Certificate Number Exists")
    .via(upsert)
    .debug("Certificate Number Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    Client.fromConfig,
    ClientConfig.default,
    Firestore.live,
    GoogleFirestoreCertificateNumberStore.layer,
    Scope.default
  )
}
