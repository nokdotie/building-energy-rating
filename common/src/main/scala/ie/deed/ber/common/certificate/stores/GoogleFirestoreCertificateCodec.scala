package ie.deed.ber.common.certificate.stores

import com.google.cloud.firestore._
import java.time.{LocalDate, Year}
import ie.deed.ber.common.certificate._
import ie.seai.ber.certificate._
import ie.eircode.ecad._
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.jdk.CollectionConverters.MapHasAsJava
import com.firebase.geofire.core.GeoHash
import zio._
import zio.stream.ZStream
import zio.gcp.firestore.{CollectionPath, DocumentPath, Firestore}
import zio.stream.ZPipeline
import ie.deed.ber.common.utils.MapUtils.getNested

object GoogleFirestoreCertificateCodec {
  val seaiIePdfCertificateField = "seai-ie-pdf-certificate"
  val eircodeIeEcadDataField = "eircode-ie-ecad-data"

  def encode(certificate: Certificate): java.util.Map[String, Any] = {
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
      case ecadData: EcadData.Found =>
        Map(
          "found" -> true,
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
      case EcadData.NotFound => Map("found" -> false).asJava
    }

    Map(
      seaiIePdfCertificateField -> seaiIePdfCertificate,
      eircodeIeEcadDataField -> eircodeIeEcadData
    ).asJava
  }

  def decode(
      id: CertificateNumber,
      map: java.util.Map[String, Any]
  ): Certificate = {
    val seaiIePdfCertificate = for {
      rating <- map
        .getNested[String](seaiIePdfCertificateField, "rating")
        .flatMap { string => Try { Rating.valueOf(string) } }
      issuedOn <- map
        .getNested[String](seaiIePdfCertificateField, "issued-on")
        .flatMap { string => Try { LocalDate.parse(string) } }
      validUntil <- map
        .getNested[String](seaiIePdfCertificateField, "valid-until")
        .flatMap { string => Try { LocalDate.parse(string) } }
      propertyAddress <- map
        .getNested[String](
          seaiIePdfCertificateField,
          "property-address"
        )
        .map { Address.apply }
      propertyEircode = map
        .getNested[String](
          seaiIePdfCertificateField,
          "property-eircode"
        )
        .map { ie.seai.ber.certificate.Eircode.apply }
        .fold(_ => None, Some(_))
      assessorNumber <- map
        .getNested[Long](seaiIePdfCertificateField, "assessor-number")
        .flatMap { long => Try { AssessorNumber(long.toInt) } }
      assessorCompanyNumber <- map
        .getNested[Long](
          seaiIePdfCertificateField,
          "assessor-company-number"
        )
        .flatMap { long => Try { AssessorCompanyNumber(long.toInt) } }
      domesticEnergyAssessmentProcedureVersion <- map
        .getNested[String](
          seaiIePdfCertificateField,
          "domestic-energy-assessment-procedure-version"
        )
        .flatMap { string =>
          Try { DomesticEnergyAssessmentProcedureVersion.valueOf(string) }
        }
      energyRating <- map
        .getNested[String](
          seaiIePdfCertificateField,
          "energy-rating-in-kWh/m2/yr"
        )
        .flatMap { string =>
          Try { KilowattHourPerSquareMetrePerYear(string.toFloat) }
        }
      carbonDioxideEmissionsIndicator <- map
        .getNested[String](
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

    val eircodeIeEcadDataFound = for {
      eircode <- map
        .getNested[String](eircodeIeEcadDataField, "eircode")
        .map { ie.eircode.ecad.Eircode.apply }
      latitude <- map
        .getNested[String](
          eircodeIeEcadDataField,
          "geographic-coordinate",
          "latitude"
        )
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Latitude.apply }
      longitude <- map
        .getNested[String](
          eircodeIeEcadDataField,
          "geographic-coordinate",
          "longitude"
        )
        .flatMap { string => Try { BigDecimal(string) } }
        .map { Longitude.apply }
      geographicCoordinate = GeographicCoordinate(latitude, longitude)
      geographicAddress <- map
        .getNested[String](
          eircodeIeEcadDataField,
          "geographic-address"
        )
        .map { GeographicAddress.apply }
      postalAddress <- map
        .getNested[String](eircodeIeEcadDataField, "postal-address")
        .map { PostalAddress.apply }
    } yield EcadData.Found(
      eircode = eircode,
      geographicCoordinate = geographicCoordinate,
      geographicAddress = geographicAddress,
      postalAddress = postalAddress
    )

    val eircodeIeEcadDataNotFound =
      map
        .getNested[Boolean](eircodeIeEcadDataField, "found")
        .collect { case false => EcadData.NotFound }

    Certificate(
      id,
      seaiIePdfCertificate.toOption,
      eircodeIeEcadDataFound.orElse(eircodeIeEcadDataNotFound).toOption
    )
  }
}
