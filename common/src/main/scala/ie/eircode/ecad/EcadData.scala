package ie.eircode.ecad

case class EcadData(
    eircode: Eircode,
    geographicCoordinate: GeographicCoordinate,
    geographicAddress: GeographicAddress,
    postalAddress: PostalAddress
)
