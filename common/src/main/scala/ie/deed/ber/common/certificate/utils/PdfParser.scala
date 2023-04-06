package ie.deed.ber.common.certificate.utils

import ie.deed.ber.common.certificate._
import java.io.File
import java.time.LocalDate
import java.awt.Rectangle
import java.time.format.DateTimeFormatter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripperByArea
import scala.util.{Try, Using}
import scala.util.matching.Regex

object PdfParser {
  val dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def getTextForRegions(document: PDDocument): String = {
    val regions = List(
      new Rectangle(10, 70, 280, 180), // Information
      new Rectangle(10, 300, 430, 450), // Building Energy Rating
      new Rectangle(490, 300, 100, 410), // Carbon Dioxide
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

  def tryFindMatch(text: String, pattern: Regex): Try[String] =
    pattern
      .findFirstMatchIn(text)
      .map { _.group(1) }
      .toRight(new Throwable(s"Missing pattern: $pattern"))
      .toTry

  def tryParse(document: PDDocument): Try[Certificate] = {
    val text = getTextForRegions(document)

    for {
      number <- tryFindMatch(text, "BER Number ([0-9]{9})".r)
        .flatMap { str => Try { str.toInt } }
        .map { CertificateNumber.apply }
      rating <- tryFindMatch(
        text,
        "BER for the building detailed below is: ([ABC][123]|[DE][12]|[FG])".r
      ).flatMap { Rating.tryFromString }
      issuedOn <- tryFindMatch(
        text,
        "Date of Issue ([0-9]{2}/[0-9]{2}/[0-9]{4})".r
      )
        .map { LocalDate.parse(_, dateTimeFormat) }
      validUntil <- tryFindMatch(
        text,
        "Valid Until ([0-9]{2}/[0-9]{2}/[0-9]{4})".r
      )
        .map { LocalDate.parse(_, dateTimeFormat) }
      propertyAddress <- tryFindMatch(text, "(?s)Address (.+)".r)
        .map { _.split("\n(Eircode|BER Number)").head.replaceAll(" +", " ") }
        .map { Address.apply }
      propertyEircode = tryFindMatch(text, "Eircode (.+)".r).map {
        Eircode.apply
      }.toOption
      assessorNumber <- tryFindMatch(text, "Assessor Number ([0-9]+)".r)
        .flatMap { str => Try { str.toInt } }
        .map { AssessorNumber.apply }
      assessorCompanyNumber <- tryFindMatch(
        text,
        "Assessor Company No ([0-9]+)".r
      )
        .flatMap { str => Try { str.toInt } }
        .map { AssessorCompanyNumber.apply }
      domesticEnergyAssessmentProcedureVersion <- tryFindMatch(
        text,
        "DEAP Version: ([0-9]\\.[0-9]\\.[0-9])".r
      )
        .flatMap { DomesticEnergyAssessmentProcedureVersion.tryFromString }
      energyRating <- tryFindMatch(text, "([0-9.]+) kWh/m²/yr".r)
        .flatMap { str => Try { str.toFloat } }
        .map { KilowattHourPerSquareMetrePerYear.apply }
      carbonDioxideEmissionsIndicator <- tryFindMatch(
        text,
        "([0-9.]+) kgCO2 /m²/yr".r
      )
        .flatMap { str => Try { str.toFloat } }
        .map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
    } yield Certificate(
      number,
      rating,
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

  def tryParse(file: File): Try[Certificate] =
    Using.resource(PDDocument.load(file))(tryParse)

}
