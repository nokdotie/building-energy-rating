package ie.nok.ecad.services

import zio.json.{DeriveJsonDecoder, JsonDecoder}

object GetEcadData {
  case class Response(
      eircodeInfo: ResponseEircodeInfo,
      postalAddress: ResponseAddress,
      geographicAddress: ResponseAddress,
      spatialInfo: ResponseSpatialInfo
  )
  given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  case class ResponseEircodeInfo(eircode: String)
  given JsonDecoder[ResponseEircodeInfo] =
    DeriveJsonDecoder.gen[ResponseEircodeInfo]

  case class ResponseAddress(english: List[String])
  given JsonDecoder[ResponseAddress] = DeriveJsonDecoder.gen[ResponseAddress]

  case class ResponseSpatialInfo(etrs89: ResponseSpatialInfoEtrs89)
  given JsonDecoder[ResponseSpatialInfo] =
    DeriveJsonDecoder.gen[ResponseSpatialInfo]

  case class ResponseSpatialInfoEtrs89(
      location: ResponseSpatialInfoEtrs89Location
  )
  given JsonDecoder[ResponseSpatialInfoEtrs89] =
    DeriveJsonDecoder.gen[ResponseSpatialInfoEtrs89]

  case class ResponseSpatialInfoEtrs89Location(
      longitude: BigDecimal,
      latitude: BigDecimal
  )
  given JsonDecoder[ResponseSpatialInfoEtrs89Location] =
    DeriveJsonDecoder.gen[ResponseSpatialInfoEtrs89Location]

  def url(key: String, addressId: String): String =
    s"https://api-finder.eircode.ie/Latest/findergetecaddata?key=$key&addressId=$addressId"

}
