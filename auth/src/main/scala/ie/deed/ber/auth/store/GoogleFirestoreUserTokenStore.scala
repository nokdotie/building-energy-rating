package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.UserToken
import ie.deed.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import zio.*
import zio.gcp.firestore.{CollectionPath, Firestore}

import scala.util.chaining.scalaUtilChainingOps

class GoogleFirestoreUserTokenStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends UserTokenStore {

  override def getUserTokenByToken(
      token: String
  ): ZIO[Any, Throwable, Option[UserToken]] = {
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(token)
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot =>
        Option.when(snapshot.exists) {
          snapshot.getData.pipe { map =>
            GoogleFirestoreUserTokenCodec.decode(token, map)
          }
        }
      }
  }
}

object GoogleFirestoreUserTokenStore {
  private val collectionPrefix = "user-token"

  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreUserTokenStore
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
      } yield GoogleFirestoreUserTokenStore(firestore, collectionPath)
    }
}
