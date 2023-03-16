import scala.util.chaining.scalaUtilChainingOps
import java.io.File
import java.time.LocalDate
import ie.seai.ber.certificate._

class PdfParserSuite extends munit.FunSuite {
  val pdfPathAndExpectedPdfCertificate = List(
    // 3.2.1
    (
      "./certificates/berCertificate_100000066.pdf",
      PdfCertificate(
        LocalDate.of(2018, 9, 2),
        LocalDate.of(2028, 9, 2),
        Address(
          "5 LAKESIDE\nOLDWOOD\nGOLFLINKS ROAD\nROSCOMMON\nCO. ROSCOMMON"
        ),
        None,
        AssessorNumber(105285),
        AssessorCompanyNumber(105284),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(139.01),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(27.93)
      )
    ),
    (
      "./certificates/berCertificate_100000181.pdf",
      PdfCertificate(
        LocalDate.of(2019, 6, 3),
        LocalDate.of(2029, 6, 3),
        Address("2 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        None,
        AssessorNumber(107153),
        AssessorCompanyNumber(107152),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(133.33),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(33.65)
      )
    ),
    (
      "./certificates/berCertificate_100000280.pdf",
      PdfCertificate(
        LocalDate.of(2016, 4, 25),
        LocalDate.of(2026, 4, 25),
        Address("8 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        None,
        AssessorNumber(105474),
        AssessorCompanyNumber(105412),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(123.36),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(29.2)
      )
    ),
    // 4.0.0
    (
      "./certificates/berCertificate_100000298.pdf",
      PdfCertificate(
        LocalDate.of(2020, 8, 15),
        LocalDate.of(2030, 8, 15),
        Address("9 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        Some(Eircode("Y25WP83")),
        AssessorNumber(104394),
        AssessorCompanyNumber(104393),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(141.54),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(31.72)
      )
    ),
    (
      "./certificates/berCertificate_100000595.pdf",
      PdfCertificate(
        LocalDate.of(2022, 1, 20),
        LocalDate.of(2032, 1, 20),
        Address("9 MIMOSA HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"),
        Some(Eircode("D18YA21")),
        AssessorNumber(101591),
        AssessorCompanyNumber(101591),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(126.6),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(23.49)
      )
    ),
    (
      "./certificates/berCertificate_100000652.pdf",
      PdfCertificate(
        LocalDate.of(2021, 10, 18),
        LocalDate.of(2031, 10, 18),
        Address("APT 13 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"),
        Some(Eircode("D18VR88")),
        AssessorNumber(103381),
        AssessorCompanyNumber(103381),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(112.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(20.8)
      )
    ),
    // 4.1.0
    (
      "./certificates/berCertificate_100000645.pdf",
      PdfCertificate(
        LocalDate.of(2022, 9, 23),
        LocalDate.of(2032, 9, 23),
        Address("APT 12 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"),
        Some(Eircode("D18AH50")),
        AssessorNumber(105792),
        AssessorCompanyNumber(105792),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(143.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.54)
      )
    ),
    (
      "./certificates/berCertificate_100000744.pdf",
      PdfCertificate(
        LocalDate.of(2023, 3, 9),
        LocalDate.of(2033, 3, 9),
        Address("APARTMENT 22 LEVMOSS HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"),
        Some(Eircode("D18V9V2")),
        AssessorNumber(103350),
        AssessorCompanyNumber(103350),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(145.67),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.44)
      )
    ),
    (
      "./certificates/berCertificate_100000967.pdf",
      PdfCertificate(
        LocalDate.of(2022, 5, 25),
        LocalDate.of(2032, 5, 25),
        Address("APT 16 THE GALLOPS\nLEVMOSS HALL\nLEOPARDSTOWN\nDUBLIN 18"),
        Some(Eircode("D18A213")),
        AssessorNumber(103530),
        AssessorCompanyNumber(103530),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(148.69),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(27.55)
      )
    )
  )

  test("should parse all certificates correctly") {
    pdfPathAndExpectedPdfCertificate.foreach {
      (pdfPath, expectedPdfCertificate) =>
        val file =
          getClass().getClassLoader().getResource(pdfPath).getFile().pipe {
            File(_)
          }
        val pdfCertificate = PdfParser.tryParse(file).get

        assertEquals(pdfCertificate, expectedPdfCertificate)
    }
  }
}
