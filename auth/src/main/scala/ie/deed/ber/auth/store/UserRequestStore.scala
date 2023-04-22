package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.UserRequest
import zio.{ZIO, ZLayer}

trait UserRequestStore {

  def saveUserRequest(userRequest: UserRequest): ZIO[Any, Throwable, Boolean]

  def getUserRequests(email: String): ZIO[Any, Throwable, List[UserRequest]]

  def countUserRequests(email: String): ZIO[Any, Throwable, Long]
}

object UserRequestStore {
  def saveUserRequest(userRequest: UserRequest): ZIO[UserRequestStore, Throwable, Boolean] =
    ZIO.serviceWithZIO[UserRequestStore] { _.saveUserRequest(userRequest) }

  def getUserRequests(email: String): ZIO[UserRequestStore, Throwable, List[UserRequest]] =
    ZIO.serviceWithZIO[UserRequestStore] { _.getUserRequests(email)}

  def countUserRequests(email: String): ZIO[UserRequestStore, Throwable, Long] =
    ZIO.serviceWithZIO[UserRequestStore] { _.countUserRequests(email) }
}

object UserRequestInMemoryStore extends UserRequestStore {

  val layer: ZLayer[Any, Throwable, UserRequestStore] =
    ZLayer.succeed(UserRequestInMemoryStore)

  private val userRequestsSet: scala.collection.mutable.HashSet[UserRequest] = scala.collection.mutable.HashSet.empty

  def saveUserRequest(userRequest: UserRequest): ZIO[Any, Throwable, Boolean] =
    ZIO.succeed(userRequestsSet.add(userRequest))

  def getUserRequests(email: String): ZIO[Any, Throwable, List[UserRequest]] =
    ZIO.succeed(userRequestsSet.filter(_._1 == email).toList)

  def countUserRequests(email: String):  ZIO[Any, Throwable, Long] =
    ZIO.succeed(userRequestsSet.count(_._1 == email))
}
