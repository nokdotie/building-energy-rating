package ie.deed.ber.common.certificate.stores

import com.google.cloud.firestore._
import ie.deed.ber.common.certificate.{Certificate, CertificateNumber}
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.util.chaining.scalaUtilChainingOps
import zio.{durationInt, System, ZIO, ZLayer}
import zio.stream.{ZPipeline, ZStream}
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}

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

  def upsertBatch(
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
      results <- firestore.commit(writeBatch)
    } yield results.size

  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int] =
    ZPipeline
      .groupedWithin[Certificate](100, 10.seconds)
      .mapZIO { chunks => upsertBatch(chunks.toList).retryN(3) }
      .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

  val streamMissingEircodeIeEcadData
      : ZStream[CertificateStore, Throwable, Certificate] =
    ZStream
      .unfoldZIO(CertificateNumber(0)) { lastCertificateNumber =>
        firestore
          .collection(collectionPath)
          .flatMap { collectionReference =>
            val query = collectionReference
              .whereGreaterThan(
                FieldPath.documentId,
                lastCertificateNumber.value.toString
              )
              .limit(100)

            ZIO.fromFutureJava {
              query.get()
            }
          }
          .map { querySnapshot =>
            querySnapshot.getDocuments.asScala
              .flatMap { snapshot =>
                val id = snapshot.getId.toInt.pipe { CertificateNumber.apply }
                scala.util
                  .Try(
                    GoogleFirestoreCertificateCodec.decode(id, snapshot.getData)
                  )
                  .toOption
              }
          }
          .map { certificates =>
            certificates.lastOption.map { lastCertificate =>
              (certificates, lastCertificate.number)
            }
          }
      }
      .takeWhile { _.nonEmpty }
      .flattenIterables

  def getByNumber(
      id: CertificateNumber
  ): ZIO[Any, Throwable, Option[Certificate]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(id.value.toString)
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot =>
        Option.when(snapshot.exists) {
          snapshot.getData.pipe {
            GoogleFirestoreCertificateCodec.decode(id, _)
          }
        }
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
