import java.io.File
import java.nio.file.StandardOpenOption.APPEND
import zio.{durationInt, Schedule, ZIO, ZIOAppDefault, ZLayer}
import zio.http.{Client, ClientConfig, Middleware}
import zio.stream.{ZPipeline, ZStream, ZSink}

val certificateNumbersStream: ZStream[Any, Nothing, Int] = {
  val smallestCertificateNumber = 100_000_000
  val biggestCertificateNumber = 200_000_000

  ZStream.range(smallestCertificateNumber, biggestCertificateNumber)
}

val collectExistingCertificateNumbers
    : ZPipeline[Client, Throwable, Int, Int] = {
  val concurrency = 25

  ZPipeline[Int]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { Option.unless(_)(certificateNumber) }
    }
    .collectSome
}

val upsertExistingCertificateNumbers: ZPipeline[Store, Throwable, Int, Unit] =
  ZPipeline.unwrap(Store.upsert)

def certificateUrl(certificateNumber: Int): String =
  s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=$certificateNumber"

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

val app: ZIO[Client with Store, Throwable, Unit] =
  certificateNumbersStream
    .debug("Certificate Number")
    .via(collectExistingCertificateNumbers)
    .debug("Certificate Number Exists")
    .via(upsertExistingCertificateNumbers)
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    ClientConfig.default,
    Client.fromConfig,
    ZLayer.succeed(FileStore)
  )
}
