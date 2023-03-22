import ie.deed.ber.common.certificate.{
  Certificate,
  CertificateNumber,
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import ie.seai.ber.certificate._
import java.io.File
import scala.util.chaining.scalaUtilChainingOps
import zio.{Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.gcp.firestore.Firestore
import zio.stream.{ZPipeline, ZSink}

def getPdfCertificate(
    certificateNumber: CertificateNumber
): ZIO[Client with ZPdfBox with Scope, Throwable, PdfCertificate] =
  for {
    file <- ZIO.attemptBlocking {
      File.createTempFile(certificateNumber.value.toString, ".pdf")
    }
    url = PdfCertificate.url(certificateNumber)
    response <- Client.request(url)
    _ <- response.body.asStream.run(ZSink.fromFile(file))
    document <- ZPdfBox.acquireRelease(file)
    certificate <- ZIO.fromTry { PdfParser.tryParse(document) }
    _ <- ZIO.attemptBlocking { file.delete }
  } yield certificate

val getCertificates: ZPipeline[
  Client with ZPdfBox with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      getPdfCertificate(certificateNumber)
        .retryN(3)
        .map { pdfCertificate =>
          Certificate(
            certificateNumber,
            None,
            Some(pdfCertificate)
          )
        }
    }
}

val upsertLimit = 1_000_000

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
    .takeWhile { _ < upsertLimit }
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    ZPdfBox.live,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Client.fromConfig,
    ClientConfig.default,
    Scope.default
  )
}
