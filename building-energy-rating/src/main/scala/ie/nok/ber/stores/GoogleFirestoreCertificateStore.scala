package ie.nok.ber.stores

import com.google.cloud.firestore.*
import ie.nok.ber.{Certificate, CertificateNumber, Eircode}
import ie.nok.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import zio.Schedule.{fixed, recurs}
import zio.stream.ZPipeline
import zio.{System, ZIO, ZLayer, durationInt}

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.chaining.scalaUtilChainingOps

class GoogleFirestoreCertificateStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateStore {

  private def filterNew(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Iterable[Certificate]] =
    certificates
      .map { certificate =>
        getByNumber(certificate.number).map { (certificate, _) }
      }
      .pipe { ZIO.collectAll }
      .map { tupleCertificates =>
        tupleCertificates
          .collect { case (certificate, None) => certificate }
      }

  protected[ber] def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Int] =
    for {
      newCertificates <- filterNew(certificates)

      collectionReference <- firestore.collection(collectionPath)
      documentReferences <- newCertificates
        .map { newCertificate =>
          firestore
            .document(
              collectionReference,
              DocumentPath(newCertificate.number.value.toString)
            )
            .map { (newCertificate, _) }
        }
        .pipe { ZIO.collectAll }
      writeBatch <- firestore.batch
      _ = documentReferences.foreach { (newCertificate, documentReference) =>
        writeBatch.set(
          documentReference,
          GoogleFirestoreCertificateCodec.encode(newCertificate)
        )
      }
      results <- firestore
        .commit(writeBatch)
        .retry(recurs(3) && fixed(1.second))
    } yield results.size

  protected[ber] val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int] =
    ZPipeline
      .groupedWithin[Certificate](100, 10.seconds)
      .mapZIO { chunks =>
        upsertBatch(chunks.toList)
          .retry(recurs(3) && fixed(1.second))
      }
      .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

  def getByNumber(
      id: CertificateNumber
  ): ZIO[Any, Throwable, Option[Certificate]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(id.value.toString)
        ZIO.fromFutureJava { query.get() }
      }
      .retry(recurs(3) && fixed(1.second))
      .map { snapshot =>
        Option(snapshot.getData)
          .map { GoogleFirestoreCertificateCodec.decode }
      }

  def getAllByEircode(
      eircode: Eircode
  ): ZIO[Any, Throwable, List[Certificate]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.whereEqualTo("eircode", eircode.value)
        ZIO.fromFutureJava { query.get() }
      }
      .retry(recurs(3) && fixed(1.second))
      .map { snapshot =>
        snapshot.getDocuments.asScala.toList
          .map { _.getData }
          .map { GoogleFirestoreCertificateCodec.decode }
      }
}

object GoogleFirestoreCertificateStore {
  val layer: ZLayer[
    Firestore.Service,
    SecurityException,
    GoogleFirestoreCertificateStore
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
      } yield GoogleFirestoreCertificateStore(firestore, collectionPath)
    }
}
