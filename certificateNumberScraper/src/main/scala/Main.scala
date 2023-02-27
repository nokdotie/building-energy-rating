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

val certificateNumbers
    : ZStream[CertificateNumberStore, Throwable, CertificateNumber] = {
  val smallestCertificateNumber = 100_000_000

  CertificateNumberStore.memento
    .map { _.fold(smallestCertificateNumber) { _.value } }
    .pipe { ZStream.fromZIO }
    .flatMap { certificateNumber =>
      ZStream
        .iterate(certificateNumber + 1)(_ + 1)
        .map { CertificateNumber.apply }
    }
}

val testExistence: ZPipeline[
  Client,
  Throwable,
  CertificateNumber,
  (CertificateNumber, Boolean)
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { (certificateNumber, _) }
    }
}

val takeWhileExists: ZPipeline[
  Any,
  Throwable,
  (CertificateNumber, Boolean),
  CertificateNumber
] = {
  val largestCertificateNumberSeen = CertificateNumber(109_500_000)
  val largestIsEmptyCountSeen = 1_000 // Not true, but more practical

  ZPipeline
    .scan[(CertificateNumber, Boolean), (CertificateNumber, Boolean, Int)](
      (CertificateNumber(0), false, 0)
    ) {
      case (_, (certificateNumber, true)) => (certificateNumber, true, 0)
      case ((_, _, isEmptyCount), (certificateNumber, false)) =>
        (certificateNumber, false, isEmptyCount + 1)
    }
    .takeWhile {
      case (certificateNumber, _, _)
          if certificateNumber.value <= largestCertificateNumberSeen.value =>
        true
      case (_, _, isEmptyCount) if isEmptyCount <= largestIsEmptyCountSeen =>
        true
      case _ => false
    }
    .collect { case (certificateNumber, true, _) => certificateNumber }
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
    .via(testExistence)
    .via(takeWhileExists)
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
