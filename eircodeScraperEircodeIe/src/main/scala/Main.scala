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

val finderEircodeIeApiKey = "_45f9ba10-677a-4ae4-a02f-02d926cae333"

def getResponse[A: JsonDecoder](url: String): ZIO[Client, Throwable, A] =
  ZyteClient
    .request(url)
    .flatMap { _.body.asString }
    .flatMap { body =>
      body
        .fromJson[A]
        .left
        .map { err => Throwable(s"$err: $body") }
        .pipe(ZIO.fromEither)
    }

def getFindAddressResponse(
    propertyAddress: String
): ZIO[Client, Throwable, FindAddress.Response] =
  FindAddress
    .url(finderEircodeIeApiKey, propertyAddress)
    .pipe(getResponse)

def getFindAddressId(
    response: FindAddress.Response
): Option[String] =
  response
    .pipe { case FindAddress.Response(addressId, addressType, options) =>
      FindAddress.ResponseOption(addressId, addressType) +: options
    }
    .collect {
      case FindAddress.ResponseOption(Some(addressId), Some(addressType))
          if addressType.text == "ResidentialAddressPoint" =>
        addressId
    }
    .pipe {
      case head :: Nil => Some(head)
      case Nil         => None
      case many        => None
    }

def getGetEcadDataResponse(
    addressId: String
): ZIO[Client, Throwable, GetEcadData.Response] =
  GetEcadData
    .url(finderEircodeIeApiKey, addressId)
    .pipe(getResponse)

def getEcadData(propertyAddress: String): ZIO[
  Client,
  Throwable,
  EcadData
] =
  getFindAddressResponse(propertyAddress)
    .map { getFindAddressId }
    .flatMap {
      case Some(addressId) => getGetEcadDataResponse(addressId).option
      case None            => ZIO.succeed(None)
    }
    .map {
      case None => EcadData.NotFound
      case Some(ecadData) =>
        EcadData.Found(
          Eircode(ecadData.eircodeInfo.eircode),
          ecadData.spatialInfo.etrs89.location.pipe { location =>
            GeographicCoordinate(
              Latitude(location.latitude),
              Longitude(location.longitude)
            )
          },
          GeographicAddress(
            ecadData.geographicAddress.english.mkString("\n")
          ),
          PostalAddress(ecadData.postalAddress.english.mkString("\n"))
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
