import ie.deed.ber.common.certificate.{
  Certificate,
  CertificateNumber,
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import scala.util.chaining.scalaUtilChainingOps
import zio._
import zio.http.{Client, ClientConfig}
import zio.gcp.firestore.Firestore
import zio.stream._
import ie.seai.ber.certificate._
import java.io.File

val certificateNumbers
    : ZStream[CertificateStore, Throwable, CertificateNumber] =
  CertificateStore.streamMissingSeaiIePdf

def getPdfCertificate(
    certificateNumber: CertificateNumber
): ZIO[Client, Throwable, PdfCertificate] =
  for {
    file <- ZIO.attemptBlocking {
      File.createTempFile(certificateNumber.value.toString, ".pdf")
    }
    url = PdfCertificate.url(certificateNumber)
    response <- Client.request(url)
    _ <- response.body.asStream.run(ZSink.fromFile(file))
    certificate <- ZIO.fromTry { PdfParser.tryParse(file) }
    _ <- ZIO.attemptBlocking { file.delete }
  } yield certificate

val getCertificates: ZPipeline[
  Client,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 5

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

val upsert: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
  ZPipeline
    .groupedWithin[Certificate](100, 10.seconds)
    .mapZIO { chunks => CertificateStore.upsertBatch(chunks.toList).retryN(3) }
    .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

val upsertLimit = 1_000

val app: ZIO[CertificateStore with Client, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(getCertificates)
    .debug("Certificate")
    .via(upsert)
    .debug("Certificate Upserted")
    .takeWhile { _ < upsertLimit }
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Client.fromConfig,
    ClientConfig.default,
    Scope.default
  )
}
