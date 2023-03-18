package ie.deed.ber.api.dao

import zio.json.{DeriveJsonCodec, JsonCodec}

import java.util.UUID

case class UserInfo(id: UUID, requestsPerDay: Int)

object UserInfo {

  implicit val codecUserInfo: JsonCodec[UserInfo] =
    DeriveJsonCodec.gen[UserInfo]
}

trait UserDao {

  def getUser(id: UUID): Option[UserInfo]
}

object UserInMemoryDao extends UserDao {

  private val userInfo = List(
    UserInfo(UUID.fromString("d2cb67e0-a686-4300-921c-3197f21e05b3"), 5),
    UserInfo(UUID.fromString("e2cb67e0-a686-4300-921c-3197f21e05b3"), 10),
    UserInfo(UUID.fromString("f2cb67e0-a686-4300-921c-3197f21e05b3"), 100)
  )

  private val userMap = userInfo.map { userInfo =>
    userInfo.id -> userInfo
  }.toMap

  override def getUser(id: UUID): Option[UserInfo] = userMap.get(id)
}
