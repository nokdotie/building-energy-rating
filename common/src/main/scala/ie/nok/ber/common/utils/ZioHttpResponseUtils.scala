package ie.nok.ber.common.utils

import java.io.File
import zio.ZIO
import zio.http.Response
import zio.stream.ZSink

object ZioHttpResponseUtils {
  def responseToFile(response: Response): ZIO[Any, Throwable, File] = for {
    file <- ZIO.attemptBlocking {
      File.createTempFile("zio-http-response", null)
    }
    _ <- response.body.asStream.run(ZSink.fromFile(file))
  } yield file
}
