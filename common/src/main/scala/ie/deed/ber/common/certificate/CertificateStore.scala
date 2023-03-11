package ie.deed.ber.common.certificate

import com.google.cloud.firestore._
import zio._
import zio.stream.ZStream
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters.{
  asScalaBufferConverter,
  mapAsJavaMapConverter,
  mapAsScalaMapConverter
}

trait CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Int]

  val streamMissingSeaiIe: ZStream[Any, Throwable, CertificateNumber]
}

object CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore](
      _.upsertBatch(certificates)
    )

  val streamMissingSeaiIe
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIe)
}

class GoogleFirestoreCertificateStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateStore {
  private val seaiIeField = "seai-ie"
  private def toMap(certificate: Certificate): java.util.Map[String, Any] =
    Map(
      "seai-ie" -> certificate.`seai.ie`.fold(null) { seaiie =>
        Map(
          "type-of-rating" -> seaiie.typeOfRating.toString,
          "issued-on" -> seaiie.issuedOn.toString,
          "valid-until" -> seaiie.validUntil.toString,
          "property-address" -> seaiie.propertyAddress.value.toString,
          "property-constructed-on" -> seaiie.propertyConstructedOn.toString,
          "property-type" -> seaiie.propertyType.toString,
          "property-floor-area" -> seaiie.propertyFloorArea.value.toString,
          "domestic-energy-assessment-procedure-version" -> seaiie.domesticEnergyAssessmentProcedureVersion.toString,
          "energy-rating" -> seaiie.energyRating.value.toString,
          "carbon-dioxide-emissions-indicator" -> seaiie.carbonDioxideEmissionsIndicator.value.toString
        ).asJava
      }
    ).asJava

  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[Any, Throwable, Int] =
    if (certificates.isEmpty) ZIO.succeed(0)
    else
      for {
        collectionReference <- firestore.collection(collectionPath)
        documentReferences <- certificates
          .map { certificate =>
            firestore
              .document(
                collectionReference,
                DocumentPath(certificate.number.value.toString)
              )
              .map { (certificate, _) }
          }
          .pipe { ZIO.collectAll }
        writeBatch <- firestore.batch
        _ = documentReferences.foreach { (certificate, documentReference) =>
          writeBatch.set(
            documentReference,
            toMap(certificate),
            SetOptions.merge
          )
        }
        results <- firestore.commit(writeBatch)
      } yield results.size

  val streamMissingSeaiIe: ZStream[Any, Throwable, CertificateNumber] = {
    val limit = 100
    ZStream
      .iterate(0)(_ + limit)
      .mapZIO { offset =>
        firestore
          .collection(collectionPath)
          .flatMap { collectionReference =>
            val query = collectionReference
              .whereEqualTo(seaiIeField, null)
              .limit(limit)
              .offset(offset)

            ZIO.fromFutureJava { query.get() }
          }
      }
      .map { querySnapshot =>
        querySnapshot.getDocuments.asScala
          .flatMap { _.getId.toIntOption }
          .map { CertificateNumber.apply }
      }
      .takeWhile { _.nonEmpty }
      .flattenIterables
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
