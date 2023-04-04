package ie.deed.ecad

case class EircodeAddressDatabaseData(
    eircode: Eircode,
    geographicCoordinate: GeographicCoordinate,
    geographicAddress: Address,
    postalAddress: Address
)
