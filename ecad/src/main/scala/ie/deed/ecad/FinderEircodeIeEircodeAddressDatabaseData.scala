package ie.deed.ecad

case class FinderEircodeIeEircodeAddressDatabaseData(
    eircode: Eircode,
    geographicCoordinate: GeographicCoordinate,
    geographicAddress: Address,
    postalAddress: Address
)
