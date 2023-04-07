import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import ie.deed.ber.common.certificate.services.NdberSeaiIePdfService
import ie.deed.ber.common.certificate.stores.{
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import zio.{Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.gcp.firestore.Firestore
import zio.stream.ZPipeline

val getCertificates: ZPipeline[
  Client with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 25

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      NdberSeaiIePdfService.getCertificate(certificateNumber)
    }
    .collectSome
}

val app: ZIO[
  CertificateStore with Client with Scope,
  Throwable,
  Unit
] =
  CertificateNumber.streamAllWithRandomStart
    .debug("Certificate Number")
    .via(getCertificates)
    .debug("Certificate")
    .via(CertificateStore.upsertPipeline)
    .debug("Certificate Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Unit] = app.provide(
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Client.fromConfig,
    ClientConfig.default,
    Scope.default
  )
}
