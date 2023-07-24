package ie.deed.ber.auth.store

import zio.ZIO

trait CreditStore {

  def getNumberOfCredits(email: String): ZIO[Any, Throwable, Long]
}

object CreditStore {
  def getNumberOfCredits(email: String): ZIO[CreditStore, Throwable, Long] = {
    ZIO.serviceWithZIO[CreditStore] {
      _.getNumberOfCredits(email)
    }
  }
}
