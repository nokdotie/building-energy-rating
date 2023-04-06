package ie.deed.ber.common.certificate.stores

import java.time.{LocalDate, Year}
import ie.deed.ber.common.certificate._
import ie.deed.ber.common.utils.MapUtils.getTyped
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.Try

object GoogleFirestoreCertificateCodec {
  def encode(certificate: Certificate): java.util.Map[String, Any] =
    Map(
      "number" -> certificate.number.value,
      "rating" -> certificate.rating.toString,
      "issued-on" -> certificate.issuedOn.toString,
      "valid-until" -> certificate.validUntil.toString,
      "address" -> certificate.propertyAddress.value,
      "eircode" -> certificate.propertyEircode.fold(null)(_.value),
      "assessor-number" -> certificate.assessorNumber.value.toLong,
      "assessor-company-number" -> certificate.assessorCompanyNumber.value.toLong,
      "domestic-energy-assessment-procedure-version" -> certificate.domesticEnergyAssessmentProcedureVersion.toString,
      "energy-rating" -> certificate.energyRating.value.toString,
      "carbon-dioxide-emissions-indicator" -> certificate.carbonDioxideEmissionsIndicator.value.toString
    ).asJava

  def decode(
      id: CertificateNumber,
      map: java.util.Map[String, Any]
  ): Certificate = (for {
    number <- map
      .getTyped[Int]("number")
      .map { CertificateNumber.apply }
    rating <- map
      .getTyped[String]("rating")
      .flatMap { string => Try { Rating.valueOf(string) } }
    issuedOn <- map
      .getTyped[String]("issued-on")
      .flatMap { string => Try { LocalDate.parse(string) } }
    validUntil <- map
      .getTyped[String]("valid-until")
      .flatMap { string => Try { LocalDate.parse(string) } }
    propertyAddress <- map
      .getTyped[String]("address")
      .map { Address.apply }
    propertyEircode = map
      .getTyped[String]("eircode")
      .map { Eircode.apply }
      .fold(_ => None, Some(_))
    assessorNumber <- map
      .getTyped[Long]("assessor-number")
      .flatMap { long => Try { AssessorNumber(long.toInt) } }
    assessorCompanyNumber <- map
      .getTyped[Long]("assessor-company-number")
      .flatMap { long => Try { AssessorCompanyNumber(long.toInt) } }
    domesticEnergyAssessmentProcedureVersion <- map
      .getTyped[String]("domestic-energy-assessment-procedure-version")
      .flatMap { string =>
        Try { DomesticEnergyAssessmentProcedureVersion.valueOf(string) }
      }
    energyRating <- map
      .getTyped[String]("energy-rating")
      .flatMap { string =>
        Try { KilowattHourPerSquareMetrePerYear(string.toFloat) }
      }
    carbonDioxideEmissionsIndicator <- map
      .getTyped[String]("carbon-dioxide-emissions-indicator")
      .flatMap { string =>
        Try { KilogramOfCarbonDioxidePerSquareMetrePerYear(string.toFloat) }
      }
  } yield Certificate(
    number = number,
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
  )).get
}
