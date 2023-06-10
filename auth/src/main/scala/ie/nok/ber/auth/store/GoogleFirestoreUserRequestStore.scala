package ie.nok.ber.auth.store

import ie.nok.ber.auth.model.UserRequest
import zio.ZIO
import zio.ZLayer
import zio.System
import zio.gcp.firestore.{CollectionPath, Firestore}

class GoogleFirestoreUserRequestStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends UserRequestStore {

  override def saveUserRequest(
      userRequest: UserRequest
  ): ZIO[Any, Throwable, String] = firestore
    .collection(collectionPath)
    .flatMap { collectionReference =>
      val data = GoogleFirestoreUserRequestCodec.encode(userRequest)
      ZIO.fromFutureJava { collectionReference.add(data) }
    }
    .map { snapshot => snapshot.getId }

  override def getUserRequests(
      email: String
  ): ZIO[Any, Throwable, List[UserRequest]] = ZIO.succeed(List.empty) // TODO

  override def countUserRequests(email: String): ZIO[Any, Throwable, Long] =
    ZIO.succeed(0) // TODO
}

object GoogleFirestoreUserRequestStore {

  private val collectionPrefix = "user-request"

  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreUserRequestStore
  ] =
    ZLayer {
      for {
        firestore <- ZIO.service[Firestore.Service]
        collectionPath <- System
          .env("ENV")
          .map {
            case Some("production") => collectionPrefix
            case _                  => s"$collectionPrefix-dev"
          }
          .map { CollectionPath.apply }
      } yield GoogleFirestoreUserRequestStore(firestore, collectionPath)
    }
}
