import java.net.URLEncoder
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object GetEcadData {
  case class Response(
      eircodeInfo: ResponseEircodeInfo,
      postalAddress: ResponseAddress,
      geographicAddress: ResponseAddress,
      spatialInfo: ResponseSpatialInfo
  )
  object Response {
    implicit val decoder: JsonDecoder[Response] =
      DeriveJsonDecoder.gen[Response]
  }

  case class ResponseEircodeInfo(
      eircode: String
  )
  object ResponseEircodeInfo {
    implicit val decoder: JsonDecoder[ResponseEircodeInfo] =
      DeriveJsonDecoder.gen[ResponseEircodeInfo]
  }

  case class ResponseAddress(
      english: List[String]
  )
  object ResponseAddress {
    implicit val decoder: JsonDecoder[ResponseAddress] =
      DeriveJsonDecoder.gen[ResponseAddress]
  }

  case class ResponseSpatialInfo(
      etrs89: ResponseSpatialInfoEtrs89
  )
  object ResponseSpatialInfo {
    implicit val decoder: JsonDecoder[ResponseSpatialInfo] =
      DeriveJsonDecoder.gen[ResponseSpatialInfo]
  }

  case class ResponseSpatialInfoEtrs89(
      location: ResponseSpatialInfoEtrs89Location
  )
  object ResponseSpatialInfoEtrs89 {
    implicit val decoder: JsonDecoder[ResponseSpatialInfoEtrs89] =
      DeriveJsonDecoder.gen[ResponseSpatialInfoEtrs89]
  }

  case class ResponseSpatialInfoEtrs89Location(
      longitude: BigDecimal,
      latitude: BigDecimal
  )
  object ResponseSpatialInfoEtrs89Location {
    implicit val decoder: JsonDecoder[ResponseSpatialInfoEtrs89Location] =
      DeriveJsonDecoder.gen[ResponseSpatialInfoEtrs89Location]
  }

  def url(key: String, addressId: String): String =
    s"https://api-finder.eircode.ie/Latest/findergetecaddata?key=$key&addressId=$addressId"
}
