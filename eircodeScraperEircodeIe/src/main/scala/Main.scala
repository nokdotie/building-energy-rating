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
import java.net.URLEncoder

val finderEircodeIeApiKey = "_45f9ba10-677a-4ae4-a02f-02d926cae333"

def getResponse[A: JsonDecoder](url: String): ZIO[Client, Throwable, A] =
  Client
    .request(url)
    .retryN(3)
    .flatMap { _.body.asString }
    .flatMap { body =>
      body.fromJson[A].left
        .map { err => Throwable(s"$err: $body") }
        .pipe(ZIO.fromEither)
    }

def getFindAddressResponse(propertyAddress: Address): ZIO[Client, Throwable, FindAddress.Response] =
  FindAddress
    .url(finderEircodeIeApiKey, propertyAddress.value)
    .pipe(getResponse)

def getFindAddressResponseOption(response: FindAddress.Response): ZIO[Any, Throwable, FindAddress.ResponseOption] =
  response
    .pipe {
      case FindAddress.Response(Some(addressId), Some(addressType), options) =>
        FindAddress.ResponseOption(addressId, addressType) +: options
      case FindAddress.Response(_, _, options) => options
    }
    .filter { _.addressType.text == "ResidentialAddressPoint" }
    .pipe {
      case head :: Nil => ZIO.succeed(head)
      case Nil => ZIO.fail(Throwable(s"No match found: $response"))
      case many => ZIO.fail(Throwable(s"Too many matches found: $response"))
    }

def getGetEcadDataResponse(option: FindAddress.ResponseOption): ZIO[Client, Throwable, FinderGetEcadData.Response] =
  GetEcadData
    .url(finderEircodeIeApiKey, option.addressId)
    .pipe(getResponse)

def getEcadData(propertyAddress: Address): ZIO[
  Client,
  Throwable,
  EcadData
] =
  getFindAddressResponse(propertyAddress)
    .flatMap { getFindAddressResponseOption }
    .flatMap { getGetEcadDataResponse }
    .map { ecadData =>
      EcadData(
        Eircode(ecadData.eircodeInfo.eircode),
        ecadData.spatialInfo.etrs89.location.pipe { location =>
          GeographicCoordinate(
            Latitude(location.latitude),
            Longitude(location.longitude),
          )
        },
        GeographicAddress(ecadData.geographicAddress.english.mkString("\n")),
        PostalAddress(ecadData.postalAddress.english.mkString("\n")),
      )
    }

val getEcad: ZPipeline[
  Client,
  Throwable,
  Certificate,
  Certificate
] = {
  val concurrency = 25

  ZPipeline[Certificate]
    .map { certificate =>
      certificate.seaiIeHtmlCertificate
        .orElse(certificate.seaiIePdfCertificate)
        .collect {
          case certificate: HtmlCertificate => certificate.propertyAddress
          case certificate: PdfCertificate => certificate.propertyAddress
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
        .option
    }
    .collectSome
}

val app: ZIO[Client with CertificateStore with Scope, Throwable, Unit] =
  CertificateStore.streamMissingEircodeIeEcadData
    .via(getEcad)
    .debug("Certificate with ECAD Data")
    .via(CertificateStore.upsertPipeline)
    .debug("Certificate Upserted")
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    ClientConfig.default, Client.fromConfig,
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Scope.default
  )
}
