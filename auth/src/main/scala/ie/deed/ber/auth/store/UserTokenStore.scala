package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.{TokenType, UserToken}
import ie.deed.ber.auth.store.UserTokenStore
import zio.{ZIO, ZLayer}

import java.time.{Instant, LocalDateTime, ZoneOffset}

trait UserTokenStore {

  def getUserTokenByToken(token: String): ZIO[Any, Throwable, Option[UserToken]]

  def isValidToken(token: String): ZIO[Any, Throwable, Boolean] = {
    getUserTokenByToken(token).map { _.nonEmpty }
  }
}

object UserTokenStore {

  def getUserByToken(
      token: String
  ): ZIO[UserTokenStore, Throwable, Option[UserToken]] = {
    ZIO.serviceWithZIO[UserTokenStore] { _.getUserTokenByToken(token) }
  }

  def isValidToken(token: String): ZIO[UserTokenStore, Throwable, Boolean] = {
    ZIO.serviceWithZIO[UserTokenStore] { _.isValidToken(token) }
  }
}

// use it locally for tests to avoid calling DB
object UserTokenInMemoryStore extends UserTokenStore {

  val layer: ZLayer[Any, Throwable, UserTokenStore] =
    ZLayer.succeed(UserTokenInMemoryStore)

  private val createdAt: Instant = LocalDateTime.of(2023, 4,1,0,0).toInstant(ZoneOffset.UTC)

  private val userTokenMapByToken: Map[String, UserToken] = Map(
    "wqerasdffv123fv342rfsd" -> Tuple2("sylweste.stocki@gmail.com", TokenType.Admin),
    "fdasgwerweereg12312vc4" -> Tuple2("P.Vinchon@gmail.com", TokenType.Admin),
    "gaaerg233432dwsv23efe2" -> Tuple2("gneotux@gmail.com", TokenType.Admin),
    "wcvopwsidfu12dsodiw23c" -> Tuple2("dev@deed.ie", TokenType.Dev),
    "rew98kerrjrjewfj3332mn" -> Tuple2("user@deed.ie", TokenType.User),
  ).map {
    case (token, (email, tokenType)) => (token, UserToken(email, token, tokenType, createdAt))
  }

  def getUserTokenByToken(token: String): ZIO[Any, Throwable, Option[UserToken]] = {
    ZIO.succeed(userTokenMapByToken.get(token))
  }
}
