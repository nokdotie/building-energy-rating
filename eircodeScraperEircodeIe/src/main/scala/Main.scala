import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import ie.deed.ber.common.certificate.stores.{
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import ie.seai.ber.certificate.{HtmlCertificate, PdfCertificate}
import ie.eircode.ecad._
import scala.util.chaining.scalaUtilChainingOps
import zio.{Scope, ZIO, ZIOAppDefault}
import zio.http.{Client, ClientConfig}
import zio.json._
import zio.gcp.firestore.Firestore
import zio.stream.ZPipeline
import ie.seai.ber.certificate.Address
import ie.deed.ber.common.utils.ZyteClient
import ie.deed.ecad.services.FinderEircodeIe

def getEcadData(propertyAddress: String): ZIO[
  Client,
  Throwable,
  EcadData
] =
  FinderEircodeIe
    .getEircodeAddressDatabaseData(propertyAddress)
    .map { ecadData =>
      EcadData.Found(
        Eircode(ecadData.eircode.value),
        GeographicCoordinate(
          Latitude(ecadData.geographicCoordinate.latitude.value),
          Longitude(ecadData.geographicCoordinate.longitude.value)
        ),
        GeographicAddress(
          ecadData.geographicAddress.value
        ),
        PostalAddress(ecadData.postalAddress.value)
      )
    }

val getEcad: ZPipeline[
  Client,
  Throwable,
  Certificate,
  Certificate
] = {
  val concurrency = 10

  ZPipeline[Certificate]
    .map { certificate =>
      certificate.seaiIePdfCertificate
        .orElse(certificate.seaiIeHtmlCertificate)
        .collect {
          case certificate: HtmlCertificate => certificate.propertyAddress.value
          case certificate: PdfCertificate =>
            certificate.propertyEircode
              .map(_.value)
              .getOrElse(certificate.propertyAddress.value)
        }
        .map { (certificate.number, _) }
    }
    .collectSome
    .mapZIOParUnordered(concurrency) { (certificateNumber, propertyAddress) =>
      getEcadData(propertyAddress)
        .map { ecadData =>
          Certificate(
            certificateNumber,
            None,
            None,
            Some(ecadData)
          )
        }
    }
}

val app: ZIO[Client with CertificateStore with Scope, Throwable, Unit] =
  CertificateStore.streamMissingEircodeIeEcadData
    .debug("Certificate")
    .via(getEcad)
    .debug("Certificate with ECAD Data")
    .via(CertificateStore.upsertPipeline)
    .debug("Certificate Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run: ZIO[Any, Throwable, Unit] = app.provide(
    ClientConfig.default,
    Client.fromConfig,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Scope.default
  )
}
