package ie.deed.ecad

case class EircodeAddressDatabaseData(
    addresses: List[Address],
    eircode: Option[Eircode],
    finderEircodeIeEcadData: Option[FinderEircodeIeEircodeAddressDatabaseData],
    finderEircodeIeEcadDataStatus: FinderEircodeIeEircodeAddressDatabaseDataStatus
)
