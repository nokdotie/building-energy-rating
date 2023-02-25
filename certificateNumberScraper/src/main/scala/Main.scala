import java.io.File
import java.nio.file.StandardOpenOption.APPEND
import zio.{durationInt, Schedule, ZIO, ZIOAppDefault, ZLayer}
import zio.http.{Client, ClientConfig, Middleware}
import zio.stream.{ZPipeline, ZStream, ZSink}

val certificateNumbers: ZStream[Any, Nothing, Int] = {
  val smallestCertificateNumber = 100_000_000

  ZStream.iterate(smallestCertificateNumber)(_ + 1)
}

val testExistence: ZPipeline[Client, Throwable, Int, Option[Int]] = {
  val concurrency = 25

  ZPipeline[Int]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { Option.when(_)(certificateNumber) }
    }
}

val takeWhileExists: ZPipeline[Any, Throwable, Option[Int], Int] = {
  val maxIsEmptyCount = 1_000

  ZPipeline
    .scan[Option[Int], (Int, Option[Int])]((0, None)) {
      case ((isEmptyCount, _), None) => (isEmptyCount + 1, None)
      case (_, certificateNumber)    => (0, certificateNumber)
    }
    .takeWhile { (isEmptyCount, _) => isEmptyCount <= maxIsEmptyCount }
    .collect { case (_, Some(certificateNumber)) => certificateNumber }
}

val upsert: ZPipeline[Store, Throwable, Int, Unit] =
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
  certificateNumbers
    .debug("Certificate Number")
    .via(testExistence)
    .via(takeWhileExists)
    .debug("Certificate Number Exists")
    .via(upsert)
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    ClientConfig.default,
    Client.fromConfig,
    ZLayer.succeed(MongoStore)
  )
}
