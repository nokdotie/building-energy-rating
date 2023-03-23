package ie.deed.ber.common.certificate.stores

import com.google.cloud.firestore._
import scala.util.chaining.scalaUtilChainingOps
import ie.deed.ber.common.certificate._
import scala.collection.JavaConverters._
import zio._
import zio.stream.ZStream
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import zio.stream.ZPipeline

class GoogleFirestoreCertificateStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateStore {

  private def copyWithDatabaseState(
      certificate: Certificate,
      databaseState: Option[Certificate]
  ): Certificate =
    certificate.copy(
      seaiIeHtmlCertificate = certificate.seaiIeHtmlCertificate.orElse(
        databaseState.flatMap(_.seaiIeHtmlCertificate)
      ),
      seaiIePdfCertificate = certificate.seaiIePdfCertificate.orElse(
        databaseState.flatMap(_.seaiIePdfCertificate)
      )
    )

  private def filterNewOrUpdated(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Iterable[Certificate]] =
    certificates
      .map { certificate =>
        getById(certificate.number).map { (certificate, _) }
      }
      .pipe { ZIO.collectAll }
      .map { tupleCertificateAndDatabaseState =>
        tupleCertificateAndDatabaseState
          .map { (certificate, databaseState) =>
            (copyWithDatabaseState(certificate, databaseState), databaseState)
          }
          .collect {
            case (certificateWithDatabaseState, databaseState)
                if !databaseState.contains(certificateWithDatabaseState) =>
              certificateWithDatabaseState
          }
      }

  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Int] =
    for {
      newOrUpdatedCertificates <- filterNewOrUpdated(certificates)

      collectionReference <- firestore.collection(collectionPath)
      documentReferences <- newOrUpdatedCertificates
        .map { newOrUpdatedCertificate =>
          firestore
            .document(
              collectionReference,
              DocumentPath(newOrUpdatedCertificate.number.value.toString)
            )
            .map { (newOrUpdatedCertificate, _) }
        }
        .pipe { ZIO.collectAll }
      writeBatch <- firestore.batch
      _ = documentReferences.foreach {
        (newOrUpdatedCertificate, documentReference) =>
          writeBatch.set(
            documentReference,
            GoogleFirestoreCertificateCodec.encode(newOrUpdatedCertificate),
            SetOptions.merge
          )
      }
      results <- firestore.commit(writeBatch)
    } yield results.size

  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int] =
    ZPipeline
      .groupedWithin[Certificate](100, 10.seconds)
      .mapZIO { chunks => upsertBatch(chunks.toList).retryN(3) }
      .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

  val streamMissingSeaiIeHtml: ZStream[Any, Throwable, CertificateNumber] =
    streamMissing(GoogleFirestoreCertificateCodec.seaiIeHtmlCertificateField)

  val streamMissingSeaiIePdf: ZStream[Any, Throwable, CertificateNumber] =
    streamMissing(GoogleFirestoreCertificateCodec.seaiIePdfCertificateField)

  private def streamMissing(
      missingField: String
  ): ZStream[Any, Throwable, CertificateNumber] =
    ZStream
      .unfoldZIO(CertificateNumber(0)) { case lastCertificateNumber =>
        firestore
          .collection(collectionPath)
          .flatMap { collectionReference =>
            val query = collectionReference
              .whereGreaterThan(
                FieldPath.documentId,
                lastCertificateNumber.value.toString
              )
              .whereEqualTo(missingField, null)
              .limit(100)

            ZIO.fromFutureJava { query.get() }
          }
          .map { querySnapshot =>
            querySnapshot.getDocuments.asScala
              .flatMap { _.getId.toIntOption }
              .map { CertificateNumber.apply }
          }
          .map { certificateNumbers =>
            certificateNumbers.lastOption.map { (certificateNumbers, _) }
          }
      }
      .takeWhile { _.nonEmpty }
      .flattenIterables

  def getById(id: CertificateNumber): ZIO[Any, Throwable, Option[Certificate]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(id.value.toString())
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
