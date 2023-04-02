package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.{ApiKeyType, UserApiKey}
import ie.deed.ber.auth.store.UserApiKeyStore
import zio.{ZIO, ZLayer}

import java.time.{Instant, LocalDateTime, ZoneOffset}

trait UserApiKeyStore {

  def getUserApiKey(apiKey: String): ZIO[Any, Throwable, Option[UserApiKey]]

  def isValidApiKey(apiKey: String): ZIO[Any, Throwable, Boolean] = {
    getUserApiKey(apiKey).map { _.nonEmpty }
  }
}

object UserApiKeyStore {

  def isValidApiKey(token: String): ZIO[UserApiKeyStore, Throwable, Boolean] = {
    ZIO.serviceWithZIO[UserApiKeyStore] { _.isValidApiKey(token) }
  }
}

// use it locally for tests to avoid calling DB
object UserApiKeyInMemoryStore extends UserApiKeyStore {

  val layer: ZLayer[Any, Throwable, UserApiKeyStore] =
    ZLayer.succeed(UserApiKeyInMemoryStore)

  private val createdAt: Instant =
    LocalDateTime.of(2023, 4, 1, 0, 0).toInstant(ZoneOffset.UTC)

  private val userTokenMapByToken: Map[String, UserApiKey] = Map(
    "wqerasdffv123fv342rfsd" -> Tuple2(
      "sylweste.stocki@gmail.com",
      ApiKeyType.Admin
    ),
    "fdasgwerweereg12312vc4" -> Tuple2("P.Vinchon@gmail.com", ApiKeyType.Admin),
    "gaaerg233432dwsv23efe2" -> Tuple2("gneotux@gmail.com", ApiKeyType.Admin),
    "wcvopwsidfu12dsodiw23c" -> Tuple2("dev@deed.ie", ApiKeyType.Dev),
    "rew98kerrjrjewfj3332mn" -> Tuple2("user@deed.ie", ApiKeyType.User)
  ).map { case (token, (email, tokenType)) =>
    (token, UserApiKey(email, token, tokenType, createdAt))
  }

  def getUserApiKey(apiKey: String): ZIO[Any, Throwable, Option[UserApiKey]] = {
    ZIO.succeed(userTokenMapByToken.get(apiKey))
  }
}
