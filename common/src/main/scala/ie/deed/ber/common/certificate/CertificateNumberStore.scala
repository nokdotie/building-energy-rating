package ie.deed.ber.common.certificate

import com.google.cloud.firestore._
import zio._
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.mapAsJavaMapConverter

trait CertificateNumberStore {
    def upsertBatch(batch: Iterable[CertificateNumber]): ZIO[Any, Throwable, Unit]
}

object CertificateNumberStore {
  def upsertBatch(batch: Iterable[CertificateNumber]): ZIO[CertificateNumberStore, Throwable, Unit] =
    ZIO.serviceWithZIO[CertificateNumberStore](_.upsertBatch(batch))
}

class GoogleFirestoreCertificateNumberStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateNumberStore {
    def upsertBatch(batch: Iterable[CertificateNumber]): ZIO[Any, Throwable, Unit] =
        for {
            collectionReference <- firestore.collection(collectionPath)
            documentReferences <- batch.map { one =>
                firestore.document(collectionReference, DocumentPath(one.value.toString))
            }.pipe { ZIO.collectAll }
            writeBatch <- firestore.batch
            _ = documentReferences.foreach { documentReference =>
                writeBatch.set(documentReference, Map.empty.asJava, SetOptions.merge)
            }
            _ <- firestore.commit(writeBatch)
        } yield ()
}

object GoogleFirestoreCertificateNumberStore {
  val layer: ZLayer[Firestore.Service, SecurityException, GoogleFirestoreCertificateNumberStore] =
    ZLayer {
      for {
        firestore <- ZIO.service[Firestore.Service]
        collectionPath <- System.env("ENV").map {
            case Some("production") => "building-energy-rating"
            case _ => "building-energy-rating-dev"
        }
        .map { CollectionPath.apply }
      } yield GoogleFirestoreCertificateNumberStore(firestore, collectionPath)
    }
}
