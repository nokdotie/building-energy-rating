import com.microsoft.playwright.Page
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, Year}
import ie.seai.ber.certificate._
import scala.util.Try

object PageParser {
  val dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  def tryParseField(page: Page, fieldName: String): Try[String] = Try {
    val selector =
      s"#ctl00_DefaultContent_BERSearch_dfBER_container_${fieldName} div"

    page
      .locator(selector)
      .innerHTML
      .replaceAll("<br>", "\n")
      .replaceAll("<sup>", "")
      .replaceAll("</sup>", "")
      .replaceAll("<sub>", "")
      .replaceAll("</sub>", "")
      .replaceAll("&nbsp;", " ")
      .replaceAll(" +", " ")
      .trim
  }

  def tryParse(page: Page): Try[HtmlCertificate] =
    for {
      typeOfRating <- tryParseField(page, "TypeOfRating")
        .flatMap { TypeOfRating.tryFromString }
      issuedOn <- tryParseField(page, "DateOfIssue")
        .map { LocalDate.parse(_, dateTimeFormat) }
      validUntil <- tryParseField(page, "DateValidUntil")
        .map { LocalDate.parse(_, dateTimeFormat) }
      address <- tryParseField(page, "PublishingAddress")
        .map { Address.apply }
      propertyConstructedOn <- tryParseField(page, "DateOfConstruction")
        .map { Year.parse }
      propertyType <- tryParseField(page, "DwellingType")
        .flatMap { PropertyType.tryFromString }
      propertyFloorArea <- tryParseField(page, "FloorArea")
        .flatMap { SquareMeter.tryFromString }
      domesticEnergyAssessmentProcedureVersion <- tryParseField(page, "BERTool")
        .flatMap { DomesticEnergyAssessmentProcedureVersion.tryFromString }
      energyRating <- tryParseField(page, "EnergyRating")
        .flatMap { KilowattHourPerSquareMetrePerYear.tryFromString }
      carbonDioxideEmissionsIndicator <- tryParseField(page, "CDERValue")
        .flatMap { KilogramOfCarbonDioxidePerSquareMetrePerYear.tryFromString }
    } yield HtmlCertificate(
      typeOfRating,
      issuedOn,
      validUntil,
      address,
      propertyConstructedOn,
      propertyType,
      propertyFloorArea,
      domesticEnergyAssessmentProcedureVersion,
      energyRating,
      carbonDioxideEmissionsIndicator
    )
}
