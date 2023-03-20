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
            GoogleFirestoreCertificateStore.toMap(certificate),
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
    streamMissing(GoogleFirestoreCertificateStore.seaiIeHtmlCertificateField)

  val streamMissingSeaiIePdf: ZStream[Any, Throwable, CertificateNumber] =
    streamMissing(GoogleFirestoreCertificateStore.seaiIePdfCertificateField)

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
            GoogleFirestoreCertificateStore.fromMap(id, _)
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

  val seaiIeHtmlCertificateField = "seai-ie-html-certificate"
  val seaiIePdfCertificateField = "seai-ie-pdf-certificate"

  def toMap(certificate: Certificate): java.util.Map[String, Any] = {
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
        "assessor-number" -> certificate.assessorNumber.value.toLong,
        "assessor-company-number" -> certificate.assessorCompanyNumber.value.toLong,
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

  def fromMap(
      id: CertificateNumber,
      map: java.util.Map[String, Any]
  ): Certificate = {
    def get[A](keys: String*): Try[A] = Try {
      keys
        .foldLeft(map: Any) { (map, key) =>
          map
            .asInstanceOf[java.util.Map[String, Any]]
            .asScala
            .get(key)
            .get
        }
        .asInstanceOf[A]
        .pipe {
          case null  => throw NullPointerException()
          case other => other
        }
    }

    val seaiIeHtmlCertificate = for {
      rating <- get[String](seaiIeHtmlCertificateField, "rating")
        .flatMap { string => Try { Rating.valueOf(string) } }
      typeOfRating <- get[String](seaiIeHtmlCertificateField, "type-of-rating")
        .flatMap { string => Try { TypeOfRating.valueOf(string) } }
      issuedOn <- get[String](seaiIeHtmlCertificateField, "issued-on")
        .flatMap { string => Try { LocalDate.parse(string) } }
      validUntil <- get[String](seaiIeHtmlCertificateField, "valid-until")
        .flatMap { string => Try { LocalDate.parse(string) } }
      propertyAddress <- get[String](
        seaiIeHtmlCertificateField,
        "property-address"
      )
        .map { Address.apply }
      propertyConstructedOn <- get[String](
        seaiIeHtmlCertificateField,
        "property-constructed-on"
      )
        .flatMap { string => Try { Year.parse(string) } }
      propertyType <- get[String](seaiIeHtmlCertificateField, "property-type")
        .flatMap { string => Try { PropertyType.valueOf(string) } }
      propertyFloorArea <- get[String](
        seaiIeHtmlCertificateField,
        "property-floor-area-in-m2"
      )
        .flatMap { string => Try { SquareMeter(string.toFloat) } }
      domesticEnergyAssessmentProcedureVersion <- get[String](
        seaiIeHtmlCertificateField,
        "domestic-energy-assessment-procedure-version"
      )
        .flatMap { string =>
          Try { DomesticEnergyAssessmentProcedureVersion.valueOf(string) }
        }
      energyRating <- get[String](
        seaiIeHtmlCertificateField,
        "energy-rating-in-kWh/m2/yr"
      )
        .flatMap { string =>
          Try { KilowattHourPerSquareMetrePerYear(string.toFloat) }
        }
      carbonDioxideEmissionsIndicator <- get[String](
        seaiIeHtmlCertificateField,
        "carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr"
      )
        .flatMap { string =>
          Try { KilogramOfCarbonDioxidePerSquareMetrePerYear(string.toFloat) }
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
    )

    val seaiIePdfCertificate = for {
      rating <- get[String](seaiIePdfCertificateField, "rating")
        .flatMap { string => Try { Rating.valueOf(string) } }
      issuedOn <- get[String](seaiIePdfCertificateField, "issued-on")
        .flatMap { string => Try { LocalDate.parse(string) } }
      validUntil <- get[String](seaiIePdfCertificateField, "valid-until")
        .flatMap { string => Try { LocalDate.parse(string) } }
      propertyAddress <- get[String](
        seaiIePdfCertificateField,
        "property-address"
      )
        .map { Address.apply }
      propertyEircode = get[String](
        seaiIePdfCertificateField,
        "property-eircode"
      )
        .map { Eircode.apply }
        .fold(_ => None, Some(_))
      assessorNumber <- get[Long](seaiIePdfCertificateField, "assessor-number")
        .flatMap { long => Try { AssessorNumber(long.toInt) } }
      assessorCompanyNumber <- get[Long](
        seaiIePdfCertificateField,
        "assessor-company-number"
      )
        .flatMap { long => Try { AssessorCompanyNumber(long.toInt) } }
      domesticEnergyAssessmentProcedureVersion <- get[String](
        seaiIePdfCertificateField,
        "domestic-energy-assessment-procedure-version"
      )
        .flatMap { string =>
          Try { DomesticEnergyAssessmentProcedureVersion.valueOf(string) }
        }
      energyRating <- get[String](
        seaiIePdfCertificateField,
        "energy-rating-in-kWh/m2/yr"
      )
        .flatMap { string =>
          Try { KilowattHourPerSquareMetrePerYear(string.toFloat) }
        }
      carbonDioxideEmissionsIndicator <- get[String](
        seaiIePdfCertificateField,
        "carbon-dioxide-emissions-indicator-in-kgCO2/m2/yr"
      )
        .flatMap { string =>
          Try { KilogramOfCarbonDioxidePerSquareMetrePerYear(string.toFloat) }
        }
    } yield PdfCertificate(
      rating = rating,
      issuedOn = issuedOn,
      validUntil = validUntil,
      propertyAddress = propertyAddress,
      propertyEircode = propertyEircode,
      assessorNumber = assessorNumber,
      assessorCompanyNumber = assessorCompanyNumber,
      domesticEnergyAssessmentProcedureVersion =
        domesticEnergyAssessmentProcedureVersion,
      energyRating = energyRating,
      carbonDioxideEmissionsIndicator = carbonDioxideEmissionsIndicator
    )

    Certificate(
      id,
      seaiIeHtmlCertificate.toOption,
      seaiIePdfCertificate.toOption
    )
  }
}
