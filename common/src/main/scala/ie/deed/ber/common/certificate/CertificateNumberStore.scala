package ie.deed.ber.common.certificate

import com.google.cloud.firestore._
import zio._
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.{
  asScalaBufferConverter,
  mapAsJavaMapConverter,
  mapAsScalaMapConverter
}

trait CertificateNumberStore {
  def upsertBatch(
      certificateNumbers: Iterable[CertificateNumber]
  ): ZIO[Any, Throwable, Int]
}

object CertificateNumberStore {
  def upsertBatch(
      certificateNumbers: Iterable[CertificateNumber]
  ): ZIO[CertificateNumberStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateNumberStore](
      _.upsertBatch(certificateNumbers)
    )
}

class GoogleFirestoreCertificateNumberStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateNumberStore {
  def upsertBatch(
      certificateNumbers: Iterable[CertificateNumber]
  ): ZIO[Any, Throwable, Int] =
    for {
      collectionReference <- firestore.collection(collectionPath)
      documentReferences <- certificateNumbers
        .map { certificateNumber =>
          firestore
            .document(
              collectionReference,
              DocumentPath(certificateNumber.value.toString)
            )
            .map { (certificateNumber, _) }
        }
        .pipe { ZIO.collectAll }
      writeBatch <- firestore.batch
      _ = documentReferences.foreach { (certificateNumber, documentReference) =>
        writeBatch.set(documentReference, Map.empty.asJava, SetOptions.merge)
      }
      results <- firestore.commit(writeBatch)
    } yield results.size
}

object GoogleFirestoreCertificateNumberStore {
  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreCertificateNumberStore
  ] =
    ZLayer {
      for {
        firestore <- ZIO.service[Firestore.Service]
        collectionPath <- System
          .env("ENV")
          .map {
            case Some("production") => "building-energy-rating"
            case _                  => "building-energy-rating-dev"
          }
          .map { CollectionPath.apply }
      } yield GoogleFirestoreCertificateNumberStore(firestore, collectionPath)
    }
}
