package ie.deed.ecad.stores

import ie.deed.ecad.{EircodeAddressDatabaseData, Eircode}
import zio.ZIO

trait EircodeAddressDatabaseDataStore {
  def upsertBatch(
      certificates: Iterable[EircodeAddressDatabaseData]
  ): ZIO[Any, Throwable, Int]

  def getByEircode(
      eircode: Eircode
  ): ZIO[Any, Throwable, Option[EircodeAddressDatabaseData]]
}

object EircodeAddressDatabaseDataStore {
  def upsertBatch(
      certificates: Iterable[EircodeAddressDatabaseData]
  ): ZIO[EircodeAddressDatabaseDataStore, Throwable, Int] =
    ZIO.serviceWithZIO[EircodeAddressDatabaseDataStore] {
      _.upsertBatch(certificates)
    }

  def getByEircode(
      eircode: Eircode
  ): ZIO[EircodeAddressDatabaseDataStore, Throwable, Option[
    EircodeAddressDatabaseData
  ]] =
    ZIO.serviceWithZIO[EircodeAddressDatabaseDataStore] {
      _.getByEircode(eircode)
    }
}
