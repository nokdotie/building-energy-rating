import java.time.LocalDate
import java.awt.Rectangle
import java.time.format.DateTimeFormatter
import ie.seai.ber.certificate._
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripperByArea
import scala.util.Try
import scala.util.matching.Regex

object PdfParser {
  val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def getTextForRegions(document: PDDocument): String = {
    val regions = List(
      new Rectangle(10, 80, 280, 180), // Information
      new Rectangle(10, 300, 430, 450), // Building Energy Rating
      new Rectangle(450, 300, 130, 410), // Carbon Dioxide
      new Rectangle(490, 820, 100, 20) // DEAP Version
    ).zipWithIndex.map { (region, index) => (index.toString, region) }

    val stripper = new PDFTextStripperByArea()
    stripper.setSortByPosition(true)
    regions.foreach { (index, region) => stripper.addRegion(index, region) }
    stripper.extractRegions(document.getPage(0))

    regions
      .map { (index, _) => stripper.getTextForRegion(index) }
      .mkString("\n")
  }

  def tryParseField(text: String, pattern: Regex): Try[String] =
    pattern
      .findFirstMatchIn(text)
      .map { _.group(1) }
      .toRight(new Throwable(s"Missing field: $pattern"))
      .toTry

  def tryParse(document: PDDocument): Try[PdfCertificate] = {
    val text = getTextForRegions(document)

    for {
      issuedOn <- tryParseField(
        text,
        "Date of Issue ([0-9]{2}/[0-9]{2}/[0-9]{4})".r
      )
        .map { LocalDate.parse(_, dateTimeFormat) }
      validUntil <- tryParseField(
        text,
        "Valid Until ([0-9]{2}/[0-9]{2}/[0-9]{4})".r
      )
        .map { LocalDate.parse(_, dateTimeFormat) }
      propertyAddress <- tryParseField(text, "(?s)Address (.+)".r)
        .map { _.split("\n(Eircode|BER Number)").head.replaceAll(" +", " ") }
        .map { Address.apply }
      propertyEircode = tryParseField(text, "Eircode (.+)".r).map {
        Eircode.apply
      }.toOption
      assessorNumber <- tryParseField(text, "Assessor Number ([0-9]+)".r)
        .flatMap { str => Try { str.toInt } }
        .map { AssessorNumber.apply }
      assessorCompanyNumber <- tryParseField(
        text,
        "Assessor Company No ([0-9]+)".r
      )
        .flatMap { str => Try { str.toInt } }
        .map { AssessorCompanyNumber.apply }
      domesticEnergyAssessmentProcedureVersion <- tryParseField(
        text,
        "DEAP Version: ([0-9]\\.[0-9]\\.[0-9])".r
      )
        .flatMap { DomesticEnergyAssessmentProcedureVersion.tryFromString }
      energyRating <- KilowattHourPerSquareMetrePerYear.tryFromString(text)
      carbonDioxideEmissionsIndicator <-
        KilogramOfCarbonDioxidePerSquareMetrePerYear.tryFromString(text)
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
  }
}
