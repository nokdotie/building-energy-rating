package ie.deed.ber.common.certificate

import com.google.cloud.firestore._
import zio._
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.{asScalaBufferConverter, mapAsJavaMapConverter, mapAsScalaMapConverter}

trait CertificateNumberStore {
  val memento: ZIO[Any, Throwable, Option[CertificateNumber]]
  def upsertBatch(certificateNumbers: Iterable[CertificateNumber]): ZIO[Any, Throwable, Unit]
}

object CertificateNumberStore {
  val memento: ZIO[CertificateNumberStore, Throwable, Option[CertificateNumber]] =
    ZIO.serviceWithZIO[CertificateNumberStore](_.memento)
  def upsertBatch(
      certificateNumbers: Iterable[CertificateNumber]
  ): ZIO[CertificateNumberStore, Throwable, Unit] =
    ZIO.serviceWithZIO[CertificateNumberStore](_.upsertBatch(certificateNumbers))
}

class GoogleFirestoreCertificateNumberStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateNumberStore {
  val certificateNumberField = "certificate-number"

  val memento: ZIO[Any, Throwable, Option[CertificateNumber]] =
    for {
        collectionReference <- firestore.collection(collectionPath)
        querySnapshot <- ZIO.fromFutureJava {
            collectionReference
                .orderBy("certificate-number", Query.Direction.DESCENDING)
                .limit(1)
                .get()
        }
        value = querySnapshot
            .getDocuments
            .asScala
            .headOption
            .flatMap { _.getData.asScala.get(certificateNumberField) }
            .map { _.asInstanceOf[Long].toInt.pipe(CertificateNumber.apply) }
    } yield value

  def upsertBatch(
      certificateNumbers: Iterable[CertificateNumber]
  ): ZIO[Any, Throwable, Unit] =
    for {
      collectionReference <- firestore.collection(collectionPath)
      documentReferences <- certificateNumbers
        .map { certificateNumber =>
          firestore.document(
            collectionReference,
            DocumentPath(certificateNumber.value.toString)
          ).map { (certificateNumber, _) }
        }
        .pipe { ZIO.collectAll }
      writeBatch <- firestore.batch
      _ = documentReferences.foreach { (certificateNumber, documentReference) =>
        val fields = Map(certificateNumberField -> certificateNumber.value)
        writeBatch.set(documentReference, fields.asJava, SetOptions.merge)
      }
      _ <- firestore.commit(writeBatch)
    } yield ()
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
