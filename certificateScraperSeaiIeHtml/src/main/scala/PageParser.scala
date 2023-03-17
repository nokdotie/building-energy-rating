import com.microsoft.playwright.Page
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, Year}
import ie.seai.ber.certificate._
import scala.util.Try
import scala.util.matching.Regex

object PageParser {
  val dateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  def tryInnerHtml(page: Page, fieldName: String): Try[String] = Try {
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

  def tryFindMatch(innerHtml: String, pattern: Regex): Try[String] =
    pattern
      .findFirstMatchIn(innerHtml)
      .map { _.group(1) }
      .toRight(new Throwable(s"Missing pattern: $pattern"))
      .toTry

  def tryParse(page: Page): Try[HtmlCertificate] =
    for {
      rating <- tryInnerHtml(page, "EnergyRating")
        .flatMap {
          tryFindMatch(
            _,
            "^([ABC][123]|[DE][12]|[FG]) [0-9.]+ \\(kWh/m2/yr\\)$".r
          )
        }
        .flatMap { Rating.tryFromString }
      typeOfRating <- tryInnerHtml(page, "TypeOfRating")
        .flatMap { TypeOfRating.tryFromString }
      issuedOn <- tryInnerHtml(page, "DateOfIssue")
        .map { LocalDate.parse(_, dateTimeFormat) }
      validUntil <- tryInnerHtml(page, "DateValidUntil")
        .map { LocalDate.parse(_, dateTimeFormat) }
      address <- tryInnerHtml(page, "PublishingAddress")
        .map { Address.apply }
      propertyConstructedOn <- tryInnerHtml(page, "DateOfConstruction")
        .map { Year.parse }
      propertyType <- tryInnerHtml(page, "DwellingType")
        .flatMap { PropertyType.tryFromString }
      propertyFloorArea <- tryInnerHtml(page, "FloorArea")
        .flatMap { tryFindMatch(_, "^([0-9.]+) \\(m2\\)$".r) }
        .flatMap { str => Try { str.toFloat } }
        .map { SquareMeter.apply }
      domesticEnergyAssessmentProcedureVersion <- tryInnerHtml(page, "BERTool")
        .flatMap { DomesticEnergyAssessmentProcedureVersion.tryFromString }
      energyRating <- tryInnerHtml(page, "EnergyRating")
        .flatMap { tryFindMatch(_, "([0-9.]+) \\(kWh/m2/yr\\)".r) }
        .flatMap { str => Try { str.toFloat } }
        .map { KilowattHourPerSquareMetrePerYear.apply }
      carbonDioxideEmissionsIndicator <- tryInnerHtml(page, "CDERValue")
        .flatMap { tryFindMatch(_, "^([0-9.]+) \\(kgCO2/m2/yr\\)$".r) }
        .flatMap { str => Try { str.toFloat } }
        .map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
    } yield HtmlCertificate(
      rating,
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
