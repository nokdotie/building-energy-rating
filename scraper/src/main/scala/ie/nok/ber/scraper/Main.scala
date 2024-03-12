package ie.nok.ber.scraper

import ie.nok.ber.{Certificate, CertificateNumber}
import ie.nok.ber.services.ndberseaiie.NdberSeaiIePdfService
import ie.nok.ber.stores.{CertificateStore, GoogleFirestoreCertificateStore}
import ie.nok.google.firestore.Firestore
import scala.util.chaining.scalaUtilChainingOps
import zio.{Console, Scope, ZIO, ZIOApp, ZIOAppArgs, EnvironmentTag}
import zio.http.{Client, ClientConfig}
import zio.stream.{ZStream, ZPipeline}

val certificateNumbersStream: ZStream[ZIOAppArgs, Throwable, CertificateNumber] =
  ZIOAppArgs.getArgs
    .map { _.headOption.flatMap { _.toIntOption }.getOrElse(0) }
    .pipe { ZStream.fromZIO }
    .flatMap { CertificateNumber.streamAllFrom(_) }

val certificatesPipeline: ZPipeline[
  Client with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 10

  ZPipeline[CertificateNumber]
    .mapZIOPar(concurrency) { certificateNumber =>
      NdberSeaiIePdfService.getCertificate(certificateNumber)
    }
    .collectSome
}

object Main extends ZIOApp {

  override type Environment = Client with GoogleFirestoreCertificateStore

  override val environmentTag = EnvironmentTag[Environment]

  override val bootstrap =
    ClientConfig.default.andTo(Client.fromConfig) ++
      Scope.default
        .andTo(Firestore.live)
        .andTo(GoogleFirestoreCertificateStore.layer)

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    certificateNumbersStream
      .via(certificatesPipeline)
      .tap { c => Console.printLine(s"Found: ${c.number.value}") }
      .via(CertificateStore.upsertPipeline)
      .tap { u => Console.printLine(s"Upserted: $u") }
      .runDrain

}
