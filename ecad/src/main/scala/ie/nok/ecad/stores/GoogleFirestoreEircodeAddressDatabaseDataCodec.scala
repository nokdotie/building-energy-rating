package ie.nok.ecad.store

import ie.nok.ber.common.utils.MapUtils.getNested
import ie.nok.ecad._
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.Try

object GoogleFirestoreEircodeAddressDatabaseDataCodec {
  def encode(
      ecadData: EircodeAddressDatabaseData
  ): java.util.Map[String, Any] =
    Map(
      "eircode" -> ecadData.eircode.value,
      "addresses" -> ecadData.addresses.map { _.value }.asJava,
      "geographic-coordinate" -> ecadData.geographicCoordinate.pipe {
        coordinate =>
          Map(
            "latitude" -> coordinate.latitude.value.toString,
            "longitude" -> coordinate.longitude.value.toString
          )
      }.asJava
    ).asJava

  def decode(map: java.util.Map[String, Any]): Try[EircodeAddressDatabaseData] =
    for {
      eircode <- map
        .getNested[String]("eircode")
        .map { Eircode.apply }
      addresses <- map
        .getNested[java.util.List[String]]("addresses")
        .map { _.asScala.toList.map { Address.apply } }
      latitude <- map
        .getNested[String]("geographic-coordinate", "latitude")
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Latitude.apply }
      longitude <- map
        .getNested[String]("geographic-coordinate", "longitude")
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Longitude.apply }
      geographicCoordinate = GeographicCoordinate(latitude, longitude)
    } yield EircodeAddressDatabaseData(
      eircode = eircode,
      addresses = addresses,
      geographicCoordinate = geographicCoordinate
    )
}
