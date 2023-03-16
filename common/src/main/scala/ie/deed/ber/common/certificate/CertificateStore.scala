package ie.deed.ber.common.certificate

import com.google.cloud.firestore._
import java.time.{LocalDate, Year}
import ie.seai.ber.certificate._
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters._
import zio._
import zio.stream.ZStream
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}

trait CertificateStore {
  def upsertBatch(certificates: Iterable[Certificate]): ZIO[Any, Throwable, Int]

  val streamMissingSeaiIe: ZStream[Any, Throwable, CertificateNumber]

  def getById(id: CertificateNumber): ZIO[Any, Throwable, Option[Certificate]]
}

object CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore] { _.upsertBatch(certificates) }

  val streamMissingSeaiIe
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIe)

  def getById(
      id: CertificateNumber
  ): ZIO[CertificateStore, Throwable, Option[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getById(id) }
}

class GoogleFirestoreCertificateStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateStore {
  private val seaiIeField = "seai-ie"
  private def toMap(certificate: Certificate): java.util.Map[String, Any] = {
    val seaiIeHtmlCertificate = certificate.seaiIeHtmlCertificate.fold(
      null
    ) { seaiie =>
      Map(
        "type-of-rating" -> seaiie.typeOfRating.toString,
        "issued-on" -> seaiie.issuedOn.toString,
        "valid-until" -> seaiie.validUntil.toString,
        "property-address" -> seaiie.propertyAddress.value,
        "property-constructed-on" -> seaiie.propertyConstructedOn.toString,
        "property-type" -> seaiie.propertyType.toString,
        "property-floor-area-in-m2" -> seaiie.propertyFloorArea.value.toString,
        "domestic-energy-assessment-procedure-version" -> seaiie.domesticEnergyAssessmentProcedureVersion.toString,
        "energy-rating-in-kWh/m2/yr" -> seaiie.energyRating.value.toString,
        "carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr" -> seaiie.carbonDioxideEmissionsIndicator.value.toString
      ).asJava
    }

    val seaiIePdfCertificate = null

    Map(
      seaiIeField -> seaiIeHtmlCertificate,
      "seai-ie-html-certificate" -> seaiIeHtmlCertificate,
      "seai-ie-pdf-certificate" -> seaiIePdfCertificate
    ).asJava
  }

  private def fromMap(
      id: CertificateNumber,
      map: java.util.Map[String, Any]
  ): Certificate =
    Certificate(
      id,
      (for {
        seaiIeField <- Try {
          map.get(seaiIeField).asInstanceOf[java.util.Map[String, Any]]
        }
        typeOfRating <- Try {
          seaiIeField.get("type-of-rating").asInstanceOf[String].pipe {
            TypeOfRating.valueOf
          }
        }
        issuedOn <- Try {
          seaiIeField.get("issued-on").asInstanceOf[String].pipe {
            LocalDate.parse
          }
        }
        validUntil <- Try {
          seaiIeField.get("valid-until").asInstanceOf[String].pipe {
            LocalDate.parse
          }
        }
        propertyAddress <- Try {
          seaiIeField.get("property-address").asInstanceOf[String].pipe {
            Address.apply
          }
        }
        propertyConstructedOn <- Try {
          seaiIeField.get("property-constructed-on").asInstanceOf[String].pipe {
            Year.parse
          }
        }
        propertyType <- Try {
          seaiIeField.get("property-type").asInstanceOf[String].pipe {
            PropertyType.valueOf
          }
        }
        propertyFloorArea <- Try {
          seaiIeField
            .get("property-floor-area-in-m2")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { SquareMeter(_) }
        }
        domesticEnergyAssessmentProcedureVersion <- Try {
          seaiIeField
            .get("domestic-energy-assessment-procedure-version")
            .asInstanceOf[String]
            .pipe { DomesticEnergyAssessmentProcedureVersion.valueOf }
        }
        energyRating <- Try {
          seaiIeField
            .get("energy-rating-in-kWh/m2/yr")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { KilowattHourPerSquareMetrePerYear.apply }
        }
        carbonDioxideEmissionsIndicator <- Try {
          seaiIeField
            .get("carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
        }
      } yield HtmlCertificate(
        typeOfRating = typeOfRating,
        issuedOn = issuedOn,
        validUntil = validUntil,
        propertyAddress = propertyAddress,
        propertyConstructedOn = propertyConstructedOn,
        propertyType = propertyType,
        propertyFloorArea = propertyFloorArea,
        domesticEnergyAssessmentProcedureVersion =
          domesticEnergyAssessmentProcedureVersion,
        energyRating = energyRating,
        carbonDioxideEmissionsIndicator = carbonDioxideEmissionsIndicator
      )).toOption,
      None
    )

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

  def getById(id: CertificateNumber): ZIO[Any, Throwable, Option[Certificate]] =
    firestore
      .collection(collectionPath)
      .flatMap { collectionReference =>
        val query = collectionReference.document(id.value.toString())
        ZIO.fromFutureJava { query.get() }
      }
      .map { snapshot =>
        Option.when(snapshot.exists) {
          snapshot.getData.pipe { fromMap(id, _) }
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
