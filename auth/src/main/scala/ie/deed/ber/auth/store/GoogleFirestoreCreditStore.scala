package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.UserRequest
import zio.{System, ZIO, ZLayer}
import zio.gcp.firestore.{CollectionPath, Firestore}
import scala.jdk.CollectionConverters.ListHasAsScala

class GoogleFirestoreCreditStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CreditStore {

  override def getNumberOfCredits(email: String): ZIO[Any, Throwable, Long] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.whereEqualTo("email", email)
        ZIO.fromFutureJava { query.get() }
      }
      .map { querySnapshot =>
        if (querySnapshot.isEmpty) {
          0 // return Zero when there are no credits for the email
        } else {
          querySnapshot.getDocuments.asScala.toList
            .map { documentSnapshot =>
              GoogleFirestoreCreditCodec.decode(documentSnapshot.getData)
            }
            .foldLeft(0L)(_ + _.number)
        }
      }
}

object GoogleFirestoreCreditStore {

  private val collectionPrefix = "credit"

  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreCreditStore
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
      } yield GoogleFirestoreCreditStore(firestore, collectionPath)
    }
}
