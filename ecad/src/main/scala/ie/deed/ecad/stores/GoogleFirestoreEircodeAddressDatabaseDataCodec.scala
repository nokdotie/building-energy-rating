package ie.deed.ecad.store

import ie.deed.ber.common.utils.MapUtils.getNested
import ie.deed.ecad._
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.util.Try

object GoogleFirestoreEircodeAddressDatabaseDataCodec {
  def encode(
      ecadData: EircodeAddressDatabaseData
  ): java.util.Map[String, Any] =
    Map(
      "eircode" -> ecadData.eircode.value,
      "geographic-coordinate" -> ecadData.geographicCoordinate.pipe {
        coordinate =>
          Map(
            "latitude" -> coordinate.latitude.value.toString,
            "longitude" -> coordinate.longitude.value.toString
          )
      }.asJava,
      "geographic-address" -> ecadData.geographicAddress.value,
      "postal-address" -> ecadData.postalAddress.value
    ).asJava

  def decode(map: java.util.Map[String, Any]): Try[EircodeAddressDatabaseData] =
    for {
      eircode <- map
        .getNested[String]("eircode")
        .map { Eircode.apply }
      latitude <- map
        .getNested[String]("geographic-coordinate", "latitude")
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Latitude.apply }
      longitude <- map
        .getNested[String]("geographic-coordinate", "longitude")
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Longitude.apply }
      geographicCoordinate = GeographicCoordinate(latitude, longitude)
      geographicAddress <- map
        .getNested[String]("geographic-address")
        .map { Address.apply }
      postalAddress <- map
        .getNested[String]("postal-address")
        .map { Address.apply }
    } yield EircodeAddressDatabaseData(
      eircode = eircode,
      geographicCoordinate = geographicCoordinate,
      geographicAddress = geographicAddress,
      postalAddress = postalAddress
    )
}
