package ie.deed.ber.common.certificate.stores

import com.google.cloud.firestore._
import java.time.{LocalDate, Year}
import ie.deed.ber.common.certificate._
import ie.seai.ber.certificate._
import ie.eircode.ecad._
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import scala.collection.JavaConverters._
import com.firebase.geofire.core.GeoHash
import zio._
import zio.stream.ZStream
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import zio.stream.ZPipeline

object GoogleFirestoreCertificateCodec {
  val seaiIeHtmlCertificateField = "seai-ie-html-certificate"
  val seaiIePdfCertificateField = "seai-ie-pdf-certificate"
  val eircodeIeEcadDataField = "eircode-ie-ecad-data"

  def encode(certificate: Certificate): java.util.Map[String, Any] = {
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

    val eircodeIeEcadData = certificate.eircodeIeEcadData.fold(null) {
      ecadData =>
        Map(
          "eircode" -> ecadData.eircode.value,
          "geographic-coordinate" -> ecadData.geographicCoordinate.pipe {
            coordinate =>
              val geohash = GeoHash(
                coordinate.latitude.value.toDouble,
                coordinate.longitude.value.toDouble
              ).getGeoHashString

              Map(
                "latitude" -> coordinate.latitude.value.toString,
                "longitude" -> coordinate.longitude.value.toString,
                "geohash" -> geohash
              )
          }.asJava,
          "geographic-address" -> ecadData.geographicAddress.value,
          "postal-address" -> ecadData.postalAddress.value
        ).asJava
    }

    Map(
      seaiIeHtmlCertificateField -> seaiIeHtmlCertificate,
      seaiIePdfCertificateField -> seaiIePdfCertificate,
      eircodeIeEcadDataField -> eircodeIeEcadData
    ).asJava
  }

  def decode(
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
        .map { ie.seai.ber.certificate.Eircode.apply }
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

    val eircodeIeEcadData = for {
      eircode <- get[String](eircodeIeEcadDataField, "eircode")
        .map { ie.eircode.ecad.Eircode.apply }
      latitude <- get[String](
        eircodeIeEcadDataField,
        "geographic-coordinate",
        "latitude"
      )
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Latitude.apply }
      longitude <- get[String](
        eircodeIeEcadDataField,
        "geographic-coordinate",
        "longitude"
      )
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Longitude.apply }
      geographicCoordinate = GeographicCoordinate(latitude, longitude)
      geographicAddress <- get[String](
        eircodeIeEcadDataField,
        "geographic-address"
      )
        .map { GeographicAddress.apply }
      postalAddress <- get[String](eircodeIeEcadDataField, "postal-address")
        .map { PostalAddress.apply }
    } yield EcadData(
      eircode = eircode,
      geographicCoordinate = geographicCoordinate,
      geographicAddress = geographicAddress,
      postalAddress = postalAddress
    )

    Certificate(
      id,
      seaiIeHtmlCertificate.toOption,
      seaiIePdfCertificate.toOption,
      eircodeIeEcadData.toOption
    )
  }
}
