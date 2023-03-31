package ie.eircode.ecad

sealed trait EcadData
object EcadData {
  case class Found(
      eircode: Eircode,
      geographicCoordinate: GeographicCoordinate,
      geographicAddress: GeographicAddress,
      postalAddress: PostalAddress
  ) extends EcadData

  case object NotFound extends EcadData
}
