package ie.deed.ber.api.token

trait UserTokenStore {

  def getUserByToken(token: String): Option[UserToken]

  def isValidToken(token: String): Boolean = getUserByToken(token).nonEmpty
}

object UserTokenInMemoryStore extends UserTokenStore {

  private val userTokenMapByToken: Map[String, UserToken] = Map(
    "wqerasdffv123fv342rfsd" -> "sylweste.stocki@gmail.com",
    "fdasgwerweereg12312vc4" -> "P.Vinchon@gmail.com",
    "gaaerg233432dwsv23efe2" -> "gneotux@gmail.com"
  ).map((token, email) => (token, UserToken(email, token)))

  def getUserByToken(token: String): Option[UserToken] =
    userTokenMapByToken.get(token)
}
