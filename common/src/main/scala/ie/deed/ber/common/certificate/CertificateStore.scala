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
import zio.stream.ZPipeline

trait CertificateStore {
  def upsertBatch(certificates: Iterable[Certificate]): ZIO[Any, Throwable, Int]
  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int]

  val streamMissingSeaiIeHtml: ZStream[Any, Throwable, CertificateNumber]
  val streamMissingSeaiIePdf: ZStream[Any, Throwable, CertificateNumber]

  def getById(id: CertificateNumber): ZIO[Any, Throwable, Option[Certificate]]
}

object CertificateStore {
  def upsertBatch(
      certificates: Iterable[Certificate]
  ): ZIO[CertificateStore, Throwable, Int] =
    ZIO.serviceWithZIO[CertificateStore] { _.upsertBatch(certificates) }

  val upsertPipeline: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
    ZPipeline.serviceWithPipeline[CertificateStore] { _.upsertPipeline }

  val streamMissingSeaiIeHtml
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIeHtml)

  val streamMissingSeaiIePdf
      : ZStream[CertificateStore, Throwable, CertificateNumber] =
    ZStream.serviceWithStream[CertificateStore](_.streamMissingSeaiIePdf)

  def getById(
      id: CertificateNumber
  ): ZIO[CertificateStore, Throwable, Option[Certificate]] =
    ZIO.serviceWithZIO[CertificateStore] { _.getById(id) }
}

class GoogleFirestoreCertificateStore(
    firestore: Firestore.Service,
    collectionPath: CollectionPath
) extends CertificateStore {
  private val seaiIeHtmlCertificateField = "seai-ie-html-certificate"
  private val seaiIePdfCertificateField = "seai-ie-pdf-certificate"

  private def toMap(certificate: Certificate): java.util.Map[String, Any] = {
    val seaiIeHtmlCertificate = certificate.seaiIeHtmlCertificate.fold(
      null
    ) { certificate =>
      Map(
        "rating" -> certificate.rating.toString,
        "type-of-rating" -> certificate.typeOfRating.toString,
        "issued-on" -> certificate.issuedOn.toString,
        "valid-until" -> certificate.validUntil.toString,
        "property-address" -> certificate.propertyAddress.value,
        "property-constructed-on" -> certificate.propertyConstructedOn.toString,
        "property-type" -> certificate.propertyType.toString,
        "property-floor-area-in-m2" -> certificate.propertyFloorArea.value.toString,
        "domestic-energy-assessment-procedure-version" -> certificate.domesticEnergyAssessmentProcedureVersion.toString,
        "energy-rating-in-kWh/m2/yr" -> certificate.energyRating.value.toString,
        "carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr" -> certificate.carbonDioxideEmissionsIndicator.value.toString
      ).asJava
    }

    val seaiIePdfCertificate = certificate.seaiIePdfCertificate.fold(
      null
    ) { certificate =>
      Map(
        "rating" -> certificate.rating.toString,
        "issued-on" -> certificate.issuedOn.toString,
        "valid-until" -> certificate.validUntil.toString,
        "property-address" -> certificate.propertyAddress.value,
        "property-eircode" -> certificate.propertyEircode.fold(null) {
          _.value
        },
        "assessor-number" -> certificate.assessorNumber.value,
        "assessor-company-number" -> certificate.assessorCompanyNumber.value,
        "domestic-energy-assessment-procedure-version" -> certificate.domesticEnergyAssessmentProcedureVersion.toString,
        "energy-rating-in-kWh/m2/yr" -> certificate.energyRating.value.toString,
        "carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr" -> certificate.carbonDioxideEmissionsIndicator.value.toString
      ).asJava
    }

    Map(
      seaiIeHtmlCertificateField -> seaiIeHtmlCertificate,
      seaiIePdfCertificateField -> seaiIePdfCertificate
    ).asJava
  }

  private def fromMap(
      id: CertificateNumber,
      map: java.util.Map[String, Any]
  ): Certificate =
    Certificate(
      id,
      (for {
        seaiIeHtmlCertificate <- Try {
          map
            .get(seaiIeHtmlCertificateField)
            .asInstanceOf[java.util.Map[String, Any]]
        }
        rating <- Try {
          seaiIeHtmlCertificate
            .get("rating")
            .asInstanceOf[String]
            .pipe { Rating.valueOf }
        }
        typeOfRating <- Try {
          seaiIeHtmlCertificate
            .get("type-of-rating")
            .asInstanceOf[String]
            .pipe {
              TypeOfRating.valueOf
            }
        }
        issuedOn <- Try {
          seaiIeHtmlCertificate.get("issued-on").asInstanceOf[String].pipe {
            LocalDate.parse
          }
        }
        validUntil <- Try {
          seaiIeHtmlCertificate.get("valid-until").asInstanceOf[String].pipe {
            LocalDate.parse
          }
        }
        propertyAddress <- Try {
          seaiIeHtmlCertificate
            .get("property-address")
            .asInstanceOf[String]
            .pipe {
              Address.apply
            }
        }
        propertyConstructedOn <- Try {
          seaiIeHtmlCertificate
            .get("property-constructed-on")
            .asInstanceOf[String]
            .pipe {
              Year.parse
            }
        }
        propertyType <- Try {
          seaiIeHtmlCertificate.get("property-type").asInstanceOf[String].pipe {
            PropertyType.valueOf
          }
        }
        propertyFloorArea <- Try {
          seaiIeHtmlCertificate
            .get("property-floor-area-in-m2")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { SquareMeter(_) }
        }
        domesticEnergyAssessmentProcedureVersion <- Try {
          seaiIeHtmlCertificate
            .get("domestic-energy-assessment-procedure-version")
            .asInstanceOf[String]
            .pipe { DomesticEnergyAssessmentProcedureVersion.valueOf }
        }
        energyRating <- Try {
          seaiIeHtmlCertificate
            .get("energy-rating-in-kWh/m2/yr")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { KilowattHourPerSquareMetrePerYear.apply }
        }
        carbonDioxideEmissionsIndicator <- Try {
          seaiIeHtmlCertificate
            .get("carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr")
            .asInstanceOf[String]
            .pipe { _.toFloat }
            .pipe { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
        }
      } yield HtmlCertificate(
        rating = rating,
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

  val upsertPipeline: ZPipeline[Any, Throwable, Certificate, Int] =
    ZPipeline
      .groupedWithin[Certificate](100, 10.seconds)
      .mapZIO { chunks => upsertBatch(chunks.toList).retryN(3) }
      .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

  val streamMissingSeaiIeHtml: ZStream[Any, Throwable, CertificateNumber] =
    streamMissing(seaiIeHtmlCertificateField)

  val streamMissingSeaiIePdf: ZStream[Any, Throwable, CertificateNumber] =
    streamMissing(seaiIePdfCertificateField)

  private def streamMissing(
      missingField: String
  ): ZStream[Any, Throwable, CertificateNumber] = {
    val limit = 100
    ZStream
      .iterate(0)(_ + limit)
      .mapZIO { offset =>
        firestore
          .collection(collectionPath)
          .flatMap { collectionReference =>
            val query = collectionReference
              .whereEqualTo(missingField, null)
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
