package ie.deed.ber.api.token

import zio.{ZIO, ZLayer}

trait UserTokenStore {

  def getUserByToken(token: String): ZIO[Any, Throwable, Option[UserToken]]

  def isValidToken(token: String): ZIO[Any, Throwable, Boolean] = {
    getUserByToken(token).map { _.nonEmpty }
  }
}

object UserTokenStore {

  def getUserByToken(
      token: String
  ): ZIO[UserTokenStore, Throwable, Option[UserToken]] = {
    ZIO.serviceWithZIO[UserTokenStore] { _.getUserByToken(token) }
  }

  def isValidToken(token: String): ZIO[UserTokenStore, Throwable, Boolean] = {
    ZIO.serviceWithZIO[UserTokenStore] { _.isValidToken(token) }
  }
}

object UserTokenInMemoryStore extends UserTokenStore {

  val layer: ZLayer[Any, Throwable, UserTokenStore] =
    ZLayer.succeed(UserTokenInMemoryStore)

  private val userTokenMapByToken: Map[String, UserToken] = Map(
    "wqerasdffv123fv342rfsd" -> "sylweste.stocki@gmail.com",
    "fdasgwerweereg12312vc4" -> "P.Vinchon@gmail.com",
    "gaaerg233432dwsv23efe2" -> "gneotux@gmail.com"
  ).map((token, email) => (token, UserToken(email, token)))

  def getUserByToken(token: String): ZIO[Any, Throwable, Option[UserToken]] = {
    ZIO.succeed(userTokenMapByToken.get(token))
  }
}
