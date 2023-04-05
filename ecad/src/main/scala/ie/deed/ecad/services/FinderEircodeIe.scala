package ie.deed.ecad.services

import ie.deed.ber.common.utils.ZyteClient
import ie.deed.ecad._
import java.net.URLEncoder
import zio.ZIO
import zio.json.{JsonDecoder, DecoderOps}
import zio.http.Client
import scala.util.chaining.scalaUtilChainingOps

object FinderEircodeIe {
  val key = "_45f9ba10-677a-4ae4-a02f-02d926cae333"

  private def request[A: JsonDecoder](url: String): ZIO[Client, Throwable, A] =
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

  private def getResidentialAddressId(
      response: FindAddress.Response
  ): ZIO[Any, Throwable, String] =
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
        case head :: Nil => ZIO.succeed(head)
        case Nil         => ZIO.fail(new Throwable("No addressId found"))
        case many        => ZIO.fail(new Throwable("Too many addressIds found"))
      }

  def getEircodeAddressDatabaseData(
      eircodeOrAddress: String
  ): ZIO[Client, Throwable, EircodeAddressDatabaseData] = for {
    findAddress <- FindAddress
      .url(key, eircodeOrAddress)
      .pipe(request[FindAddress.Response])
    addressId <- getResidentialAddressId(findAddress)
    getEcadData <- GetEcadData
      .url(key, addressId)
      .pipe(request[GetEcadData.Response])
  } yield EircodeAddressDatabaseData(
    eircode = Eircode(getEcadData.eircodeInfo.eircode),
    geographicCoordinate =
      getEcadData.spatialInfo.etrs89.location.pipe { location =>
        GeographicCoordinate(
          longitude = Longitude(location.longitude),
          latitude = Latitude(location.latitude)
        )
      },
    geographicAddress =
      Address(getEcadData.geographicAddress.english.mkString("\n")),
    postalAddress = Address(getEcadData.postalAddress.english.mkString("\n"))
  )
}
