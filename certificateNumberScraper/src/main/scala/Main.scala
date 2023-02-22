import java.io.File
import java.nio.file.StandardOpenOption.APPEND
import zio.{durationInt, Schedule, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig, Middleware}
import zio.stream.{ZStream, ZSink}

val certificateNumbersStream: ZStream[Any, Nothing, Int] = {
  val smallestCertificateNumber = 100_000_000
  val biggestCertificateNumber = 200_000_000

  ZStream.range(smallestCertificateNumber, biggestCertificateNumber)
}

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

val app: ZIO[Client, Throwable, String] = {
  val concurrency = 25
  val file = File.createTempFile("certificate-numbers-", null)

  certificateNumbersStream
    .debug("Certificate Number")
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      val url = certificateUrl(certificateNumber)

      resourceExists(url)
        .map { Option.unless(_)(certificateNumber) }
    }
    .collectSome
    .debug("Certificate Number Exists")
    .map { _.toString }
    .intersperse("\n")
    .map(_.getBytes.toList)
    .flattenIterables
    .run { ZSink.fromFile(file).map { _ => file.getAbsolutePath } }
    .debug("File")
}

object Main extends ZIOAppDefault {
  def run = app.provide(ClientConfig.default, Client.fromConfig)
}
