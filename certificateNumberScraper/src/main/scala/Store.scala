import zio.{Console, ZIO}
import zio.stream.{ZPipeline, ZSink}
import java.io.{File, IOException}

trait Store {
  val upsert: ZPipeline[Any, Throwable, Int, Unit]
}

object Store {
  val upsert: ZIO[Store, Nothing, ZPipeline[Any, Throwable, Int, Unit]] =
    ZIO.serviceWith[Store] { _.upsert }
}

object FileStore extends Store {
  val upsert: ZPipeline[Any, Throwable, Int, Unit] = {
    val file = File.createTempFile("certificate-numbers-", ".txt")

    ZPipeline.fromSink {
      ZSink
        .fromFile(file)
        .contramapChunks { certificateNumbers =>
          certificateNumbers.flatMap { certificateNumber =>
            s"$certificateNumber\n".getBytes
          }
        }
        .summarized {
          Console.printLine(s"Upsert in ${file.getAbsolutePath}")
        } { (_, _) => () }
        .map { _ => () }
        .ignoreLeftover
    }
  }
}
