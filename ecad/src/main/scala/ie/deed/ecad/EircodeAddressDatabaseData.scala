package ie.deed.ecad

import scala.util.chaining.scalaUtilChainingOps

sealed abstract case class EircodeAddressDatabaseData private (
    eircode: Eircode,
    addresses: List[Address],
    geographicCoordinate: GeographicCoordinate
)

object EircodeAddressDatabaseData {
  def apply(
      eircode: Eircode,
      addresses: List[Address],
      geographicCoordinate: GeographicCoordinate
  ): EircodeAddressDatabaseData =
    new EircodeAddressDatabaseData(
      eircode = eircode,
      addresses = addresses.distinct,
      geographicCoordinate = geographicCoordinate
    ) {}
}
