import java.io.File
import java.nio.file.StandardOpenOption.APPEND
import zio.{durationInt, Schedule, ZIO, ZIOAppDefault, ZLayer}
import zio.http.{Client, ClientConfig, Middleware}
import zio.stream.{ZPipeline, ZStream, ZSink}

val certificateNumbers: ZStream[Any, Nothing, Int] = {
  val smallestCertificateNumber = 100_000_000
  ZStream.iterate(smallestCertificateNumber)(_ + 1)
}

val testExistence: ZPipeline[Client, Throwable, Int, (Int, Boolean)] = {
  val concurrency = 25

  ZPipeline[Int]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { (certificateNumber, _) }
    }
}

val takeWhileExists: ZPipeline[Any, Throwable, (Int, Boolean), Int] = {
  val largestCertificateNumberSeen = 109_500_000
  val largestIsEmptyCountSeen = 1_000 // Not true, but more practical

  ZPipeline
    .scan[(Int, Boolean), (Int, Boolean, Int)]((0, false, 0)) {
      case (_, (certificateNumber, true)) => (certificateNumber, true, 0)
      case ((_, _, isEmptyCount), (certificateNumber, false)) =>
        (certificateNumber, false, isEmptyCount + 1)
    }
    .takeWhile {
      case (certificateNumber, _, _)
          if certificateNumber <= largestCertificateNumberSeen =>
        true
      case (_, _, isEmptyCount) if isEmptyCount <= largestIsEmptyCountSeen =>
        true
      case _ => false
    }
    .collect { case (certificateNumber, true, _) => certificateNumber }
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
