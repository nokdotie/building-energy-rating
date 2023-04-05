import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import zio._

trait ZPdfBox {
  def acquireRelease(file: File): ZIO[Scope, Throwable, PDDocument]
}

object ZPdfBox {

  def acquireRelease(
      file: File
  ): ZIO[ZPdfBox with Scope, Throwable, PDDocument] =
    ZIO.serviceWithZIO[ZPdfBox] { _.acquireRelease(file) }

  def live: ZLayer[Any, Throwable, ZPdfBox] = ZLayer.succeed {
    new ZPdfBox {
      def acquireRelease(file: File) = ZIO
        .fromAutoCloseable(ZIO.attempt { PDDocument.load(file) })
    }
  }

}
