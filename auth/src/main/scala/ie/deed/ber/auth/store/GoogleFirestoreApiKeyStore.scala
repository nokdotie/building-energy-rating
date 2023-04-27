package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.ApiKey
import ie.deed.ber.common.certificate.stores.GoogleFirestoreCertificateStore
import zio.ZIO
import zio.ZLayer
import zio.System
import zio.gcp.firestore.{CollectionPath, Firestore}

import scala.util.chaining.scalaUtilChainingOps

class GoogleFirestoreApiKeyStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends ApiKeyStore {

  override def getApiKey(
      apiKey: String
  ): ZIO[Any, Throwable, Option[ApiKey]] = {
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(apiKey)
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot =>
        Option.when(snapshot.exists) {
          snapshot.getData.pipe { map =>
            GoogleFirestoreApiKeyCodec.decode(apiKey, map)
          }
        }
      }
  }
}

object GoogleFirestoreApiKeyStore {
  private val collectionPrefix = "api-key"

  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreApiKeyStore
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
      } yield GoogleFirestoreApiKeyStore(firestore, collectionPath)
    }
}
