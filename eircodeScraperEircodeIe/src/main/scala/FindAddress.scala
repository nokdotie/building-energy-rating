import java.net.URLEncoder
import zio.json.{DeriveJsonDecoder, JsonDecoder}

object FindAddress {
  case class Response(
      addressId: Option[String],
      addressType: Option[ResponseAddressType],
      options: List[ResponseOption]
  )
  object Response {
    implicit val decoder: JsonDecoder[Response] =
      DeriveJsonDecoder.gen[Response]
  }

  case class ResponseAddressType(
      text: String
  )
  object ResponseAddressType {
    implicit val decoder: JsonDecoder[ResponseAddressType] =
      DeriveJsonDecoder.gen[ResponseAddressType]
  }

  case class ResponseOption(
      addressId: String,
      addressType: Option[ResponseAddressType]
  )
  object ResponseOption {
    implicit val decoder: JsonDecoder[ResponseOption] =
      DeriveJsonDecoder.gen[ResponseOption]
  }

  def url(key: String, address: String): String = {
    val urlEncodedAddress = URLEncoder.encode(address, "UTF-8")
    s"https://api-finder.eircode.ie/Latest/finderfindaddress?key=$key&address=$urlEncodedAddress"
  }
}
