import scala.util.{Try, Using}
import java.io.File
import java.time.LocalDate
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text._
import java.awt.Rectangle
import java.time.format.DateTimeFormatter
import scala.util.matching.Regex
import ie.seai.ber.certificate._

object PdfParser {
  val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def tryParseField(pattern: Regex, blob: String): Try[String] =
    pattern
      .findFirstMatchIn(blob)
      .map { _.group(1) }
      .toRight(new Throwable(s"Missing field: $pattern"))
      .toTry

  def tryParse(file: File): Try[PdfCertificate] =
    Using(PDDocument.load(file)) { document =>
      val stripper = new PDFTextStripperByArea()
      stripper.setSortByPosition(true)

      stripper.addRegion("Information", new Rectangle(10, 80, 280, 180))
      stripper.addRegion(
        "Building Energy Rating",
        new Rectangle(10, 300, 430, 450)
      )
      stripper.addRegion("Carbon Dioxide", new Rectangle(450, 300, 130, 410))
      stripper.addRegion(
        "Domestic Energy Assessment Procedure Version",
        new Rectangle(490, 820, 100, 20)
      )

      stripper.extractRegions(document.getPage(0))

      val informationRegion = stripper.getTextForRegion("Information")
      val buildingEnergyRatingRegion =
        stripper.getTextForRegion("Building Energy Rating")
      val carbonDioxideRegion = stripper.getTextForRegion("Carbon Dioxide")
      val domesticEnergyAssessmentProcedureVersionRegion =
        stripper.getTextForRegion(
          "Domestic Energy Assessment Procedure Version"
        )

      for {
        issuedOn <- tryParseField(
          "Date of Issue ([0-9]{2}/[0-9]{2}/[0-9]{4})".r,
          informationRegion
        )
          .map { LocalDate.parse(_, dateTimeFormat) }
        validUntil <- tryParseField(
          "Valid Until ([0-9]{2}/[0-9]{2}/[0-9]{4})".r,
          informationRegion
        )
          .map { LocalDate.parse(_, dateTimeFormat) }
        propertyAddress <- tryParseField(
          "(?s)Address (.+)".r,
          informationRegion
        )
          .map { _.split("\n(Eircode|BER Number)").head.replaceAll(" +", " ") }
          .map { Address.apply }
        propertyEircode = tryParseField(
          "Eircode (.+)".r,
          informationRegion
        ).map { Eircode.apply }.toOption
        assessorNumber <- tryParseField(
          "Assessor Number ([0-9]+)".r,
          informationRegion
        )
          .flatMap { str => Try { str.toInt } }
          .map { AssessorNumber.apply }
        assessorCompanyNumber <- tryParseField(
          "Assessor Company No ([0-9]+)".r,
          informationRegion
        )
          .flatMap { str => Try { str.toInt } }
          .map { AssessorCompanyNumber.apply }
        domesticEnergyAssessmentProcedureVersion <- tryParseField(
          "DEAP Version: ([0-9]\\.[0-9]\\.[0-9])".r,
          domesticEnergyAssessmentProcedureVersionRegion
        )
          .flatMap { DomesticEnergyAssessmentProcedureVersion.tryFromString }
        energyRating <- KilowattHourPerSquareMetrePerYear.tryFromString(
          buildingEnergyRatingRegion
        )
        carbonDioxideEmissionsIndicator <-
          KilogramOfCarbonDioxidePerSquareMetrePerYear.tryFromString(
            carbonDioxideRegion
          )
      } yield PdfCertificate(
        issuedOn,
        validUntil,
        propertyAddress,
        propertyEircode,
        assessorNumber,
        assessorCompanyNumber,
        domesticEnergyAssessmentProcedureVersion,
        energyRating,
        carbonDioxideEmissionsIndicator
      )
    }.flatten
}
