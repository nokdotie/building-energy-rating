package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{ApiKey, ApiKeyType}
import ie.deed.ber.auth.store.ApiKeyStore
import zio.{ZIO, ZLayer}

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.{Calendar, Date}

trait ApiKeyStore {

  def getApiKey(apiKey: String): ZIO[Any, Throwable, Option[ApiKey]]

  def isValidApiKey(apiKey: String): ZIO[Any, Throwable, Boolean] = {
    getApiKey(apiKey).map { _.nonEmpty }
  }
}

object ApiKeyStore {

  def isValidApiKey(apiKey: String): ZIO[ApiKeyStore, Throwable, Boolean] = {
    ZIO.serviceWithZIO[ApiKeyStore] { _.isValidApiKey(apiKey) }
  }

  def getApiKey(apiKey: String): ZIO[ApiKeyStore, Throwable, Option[ApiKey]] = {
    ZIO.serviceWithZIO[ApiKeyStore] { _.getApiKey(apiKey) }
  }
}

// use it locally for tests to avoid calling DB
object ApiKeyInMemoryStore extends ApiKeyStore {

  val layer: ZLayer[Any, Throwable, ApiKeyStore] =
    ZLayer.succeed(ApiKeyInMemoryStore)

  private val createdAt: Instant =
    LocalDateTime.of(2023, 4, 1, 0, 0).toInstant(ZoneOffset.UTC)

  private val apiKeyMap: Map[String, ApiKey] = Map(
    "wqerasdffv123fv342rfsd" -> Tuple2(
      "sylweste.stocki@gmail.com",
      ApiKeyType.Admin
    ),
    "fdasgwerweereg12312vc4" -> Tuple2("P.Vinchon@gmail.com", ApiKeyType.Admin),
    "gaaerg233432dwsv23efe2" -> Tuple2("gneotux@gmail.com", ApiKeyType.Admin),
    "wcvopwsidfu12dsodiw23c" -> Tuple2("dev@deed.ie", ApiKeyType.Dev),
    "rew98kerrjrjewfj3332mn" -> Tuple2("user@deed.ie", ApiKeyType.User)
  ).map { case (apiKey, (email, apiKeyType)) =>
    (apiKey, ApiKey(email, apiKey, apiKeyType, createdAt))
  }

  def getApiKey(apiKey: String): ZIO[Any, Throwable, Option[ApiKey]] = {
    ZIO.succeed(apiKeyMap.get(apiKey))
  }
}
