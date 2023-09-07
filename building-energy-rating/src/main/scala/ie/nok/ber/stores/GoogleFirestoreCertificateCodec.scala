package ie.nok.ber.stores

import ie.nok.ber._
import ie.nok.ber.services.ndberseaiie.NdberSeaiIePdfService
import java.time.{LocalDate, Year}
import java.util.{Map => JMap}
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

protected[stores] object GoogleFirestoreCertificateCodec {
  private def getTyped[A](map: JMap[String, Any], key: String): Try[A] = Try {
    map
      .get(key)
      .pipe { Option.apply }
      .get
      .asInstanceOf[A]
  }

  def encode(certificate: Certificate): JMap[String, Any] =
    Map(
      "url" -> certificate.url,
      "number" -> certificate.number.value.toLong,
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

  def decode(map: JMap[String, Any]): Certificate = (for {
    number <- getTyped[Long](map, "number")
      .map { _.toInt }
      .map { CertificateNumber.apply }
    rating <- getTyped[String](map, "rating")
      .flatMap { string => Try { Rating.valueOf(string) } }
    issuedOn <- getTyped[String](map, "issued-on")
      .flatMap { string => Try { LocalDate.parse(string) } }
    validUntil <- getTyped[String](map, "valid-until")
      .flatMap { string => Try { LocalDate.parse(string) } }
    propertyAddress <- getTyped[String](map, "address")
      .map { Address.fromString }
    propertyEircode = getTyped[String](map, "eircode").map {
      Eircode.fromString
    }.toOption
    assessorNumber <- getTyped[Long](map, "assessor-number")
      .flatMap { long => Try { AssessorNumber(long.toInt) } }
    assessorCompanyNumber <- getTyped[Long](map, "assessor-company-number")
      .flatMap { long => Try { AssessorCompanyNumber(long.toInt) } }
    domesticEnergyAssessmentProcedureVersion <- getTyped[String](
      map,
      "domestic-energy-assessment-procedure-version"
    )
      .flatMap { string =>
        Try { DomesticEnergyAssessmentProcedureVersion.valueOf(string) }
      }
    energyRating <- getTyped[String](map, "energy-rating")
      .flatMap { string =>
        Try { KilowattHourPerSquareMetrePerYear(string.toFloat) }
      }
    carbonDioxideEmissionsIndicator <- getTyped[String](
      map,
      "carbon-dioxide-emissions-indicator"
    )
      .flatMap { string =>
        Try { KilogramOfCarbonDioxidePerSquareMetrePerYear(string.toFloat) }
      }
    url = getTyped[String](map, "url")
      .getOrElse { NdberSeaiIePdfService.getUrl(number) }
  } yield Certificate(
    url = url,
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
