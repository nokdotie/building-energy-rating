import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import ie.deed.ber.common.certificate.stores.{
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import ie.seai.ber.certificate._
import java.io.File
import scala.util.chaining.scalaUtilChainingOps
import zio.{Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.http.model.HeaderValues.applicationOctetStream
import zio.gcp.firestore.Firestore
import zio.stream.{ZPipeline, ZSink}

def getPdfCertificate(
    certificateNumber: CertificateNumber
): ZIO[Client with ZPdfBox with Scope, Throwable, PdfCertificate] = {
  val url = PdfCertificate.url(certificateNumber)

  val createTempPdfFile = ZIO.attemptBlocking {
    File.createTempFile(certificateNumber.value.toString, ".pdf")
  }
  val deleteTempPdfFile = (file: File) =>
    ZIO.attemptBlocking { file.delete }.orDie

  for {
    response <- Client
      .request(url)
      .filterOrFail { _.hasContentType(applicationOctetStream) } {
        new Throwable("Invalid format")
      }
      .retryN(3)
    file <- ZIO.acquireRelease(createTempPdfFile)(deleteTempPdfFile)
    _ <- response.body.asStream.run(ZSink.fromFile(file))
    document <- ZPdfBox.acquireRelease(file)
    certificate <- ZIO
      .fromTry { PdfParser.tryParse(document) }
      .logError("PDF Parser Failed")
  } yield certificate
}

val getCertificates: ZPipeline[
  Client with ZPdfBox with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      getPdfCertificate(certificateNumber).map { pdfCertificate =>
        Certificate(
          certificateNumber,
          None,
          Some(pdfCertificate),
          None
        )
      }.option
    }
    .collectSome
}

val app: ZIO[
  CertificateStore with Client with ZPdfBox with Scope,
  Throwable,
  Unit
] =
  CertificateStore.streamMissingSeaiIePdf
    .debug("Certificate Number")
    .via(getCertificates)
    .debug("Certificate")
    .via(CertificateStore.upsertPipeline)
    .debug("Certificate Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Unit] = app.provide(
    ZPdfBox.live,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Client.fromConfig,
    ClientConfig.default,
    Scope.default
  )
}
