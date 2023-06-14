package ie.nok.ecad.services

import java.net.URLEncoder
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object FindAddress {
  case class Response(
      addressId: Option[String],
      addressType: Option[ResponseAddressType],
      options: List[ResponseOption]
  )
  given JsonDecoder[Response] = DeriveJsonDecoder.gen[Response]

  case class ResponseAddressType(
      text: String
  )
  given JsonDecoder[ResponseAddressType] =
    DeriveJsonDecoder.gen[ResponseAddressType]

  case class ResponseOption(
      addressId: Option[String],
      addressType: Option[ResponseAddressType]
  )
  given JsonDecoder[ResponseOption] = DeriveJsonDecoder.gen[ResponseOption]

  def url(key: String, address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://api-finder.eircode.ie/Latest/finderfindaddress?key=$key&address=$urlEncodedAddress"
  }

}
