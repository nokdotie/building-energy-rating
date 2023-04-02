package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.UserApiKey
import ie.deed.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import zio.*
import zio.gcp.firestore.{CollectionPath, Firestore}

import scala.util.chaining.scalaUtilChainingOps

class GoogleFirestoreUserApiKeyStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends UserApiKeyStore {

  override def getUserApiKey(
      apiKey: String
  ): ZIO[Any, Throwable, Option[UserApiKey]] = {
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(apiKey)
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot =>
        Option.when(snapshot.exists) {
          snapshot.getData.pipe { map =>
            GoogleFirestoreUserTokenCodec.decode(apiKey, map)
          }
        }
      }
  }
}

object GoogleFirestoreUserApiKeyStore {
  private val collectionPrefix = "user-token"

  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreUserApiKeyStore
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
      } yield GoogleFirestoreUserApiKeyStore(firestore, collectionPath)
    }
}
