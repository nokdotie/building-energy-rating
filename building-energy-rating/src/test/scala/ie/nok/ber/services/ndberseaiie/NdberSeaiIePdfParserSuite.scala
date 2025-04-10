package ie.nok.ber.services.ndberseaiie

import ie.nok.ber._
import java.io.File
import java.time.LocalDate
import org.apache.pdfbox.pdmodel.PDDocument
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

class NdberSeaiIePdfParserSuite extends munit.FunSuite {
  val pdfPathAndExpectedCertificate = List(
    // 3.2.1
    (
      "./ndberseaiie/100000066.pdf",
      Certificate(
        "",
        CertificateNumber(100000066),
        Rating.B3,
        LocalDate.of(2018, 9, 2),
        LocalDate.of(2028, 9, 2),
        Address.fromString(
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
      "./ndberseaiie/100000181.pdf",
      Certificate(
        "",
        CertificateNumber(100000181),
        Rating.B3,
        LocalDate.of(2019, 6, 3),
        LocalDate.of(2029, 6, 3),
        Address.fromString("2 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        None,
        AssessorNumber(107153),
        AssessorCompanyNumber(107152),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(133.33),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(33.65)
      )
    ),
    (
      "./ndberseaiie/100000280.pdf",
      Certificate(
        "",
        CertificateNumber(100000280),
        Rating.B2,
        LocalDate.of(2016, 4, 25),
        LocalDate.of(2026, 4, 25),
        Address.fromString("8 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
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
      "./ndberseaiie/100000298.pdf",
      Certificate(
        "",
        CertificateNumber(100000298),
        Rating.B3,
        LocalDate.of(2020, 8, 15),
        LocalDate.of(2030, 8, 15),
        Address.fromString("9 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        Some(Eircode.fromString("Y25WP83")),
        AssessorNumber(104394),
        AssessorCompanyNumber(104393),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(141.54),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(31.72)
      )
    ),
    (
      "./ndberseaiie/100000595.pdf",
      Certificate(
        "",
        CertificateNumber(100000595),
        Rating.B3,
        LocalDate.of(2022, 1, 20),
        LocalDate.of(2032, 1, 20),
        Address.fromString(
          "9 MIMOSA HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"
        ),
        Some(Eircode.fromString("D18YA21")),
        AssessorNumber(101591),
        AssessorCompanyNumber(101591),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(126.6),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(23.49)
      )
    ),
    (
      "./ndberseaiie/100000652.pdf",
      Certificate(
        "",
        CertificateNumber(100000652),
        Rating.B2,
        LocalDate.of(2021, 10, 18),
        LocalDate.of(2031, 10, 18),
        Address.fromString(
          "APT 13 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"
        ),
        Some(Eircode.fromString("D18VR88")),
        AssessorNumber(103381),
        AssessorCompanyNumber(103381),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(112.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(20.8)
      )
    ),
    // 4.1.0
    (
      "./ndberseaiie/100000645.pdf",
      Certificate(
        "",
        CertificateNumber(100000645),
        Rating.B3,
        LocalDate.of(2022, 9, 23),
        LocalDate.of(2032, 9, 23),
        Address.fromString(
          "APT 12 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"
        ),
        Some(Eircode.fromString("D18AH50")),
        AssessorNumber(105792),
        AssessorCompanyNumber(105792),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(143.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.54)
      )
    ),
    (
      "./ndberseaiie/100000744.pdf",
      Certificate(
        "",
        CertificateNumber(100000744),
        Rating.B3,
        LocalDate.of(2023, 3, 9),
        LocalDate.of(2033, 3, 9),
        Address.fromString(
          "APARTMENT 22 LEVMOSS HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"
        ),
        Some(Eircode.fromString("D18V9V2")),
        AssessorNumber(103350),
        AssessorCompanyNumber(103350),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(145.67),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.44)
      )
    ),
    (
      "./ndberseaiie/100000967.pdf",
      Certificate(
        "",
        CertificateNumber(100000967),
        Rating.B3,
        LocalDate.of(2022, 5, 25),
        LocalDate.of(2032, 5, 25),
        Address.fromString(
          "APT 16 THE GALLOPS\nLEVMOSS HALL\nLEOPARDSTOWN\nDUBLIN 18"
        ),
        Some(Eircode.fromString("D18A213")),
        AssessorNumber(103530),
        AssessorCompanyNumber(103530),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(148.69),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(27.55)
      )
    ),
    (
      "./ndberseaiie/100290303.pdf",
      Certificate(
        "",
        CertificateNumber(100290303),
        Rating.E2,
        LocalDate.of(2014, 10, 24),
        LocalDate.of(2024, 10, 24),
        Address.fromString("17 GALLEN VIEW\nFERBANE\nBIRR\nCO. OFFALY"),
        None,
        AssessorNumber(102657),
        AssessorCompanyNumber(102653),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(340.28),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(109.08)
      )
    ),
    (
      "./ndberseaiie/106559123.pdf",
      Certificate(
        "",
        CertificateNumber(106559123),
        Rating.F,
        LocalDate.of(2014, 6, 28),
        LocalDate.of(2024, 6, 28),
        Address.fromString("RHINE\nKILLOE\nCO. LONGFORD"),
        None,
        AssessorNumber(105323),
        AssessorCompanyNumber(105323),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(442.7),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(143.21)
      )
    ),
    (
      "./ndberseaiie/106559149.pdf",
      Certificate(
        "",
        CertificateNumber(106559149),
        Rating.G,
        LocalDate.of(2014, 6, 29),
        LocalDate.of(2024, 6, 29),
        Address.fromString("303 CONNOLLY ROAD\nPORTLAW\nCO. WATERFORD"),
        None,
        AssessorNumber(104559),
        AssessorCompanyNumber(104559),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(451.65),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(140.69)
      )
    )
  )

  test("should parse all certificates correctly") {
    pdfPathAndExpectedCertificate.foreach { (pdfPath, expectedCertificate) =>
      val file = getClass()
        .getClassLoader()
        .getResource(pdfPath)
        .getFile()
        .pipe { File(_) }

      val Certificate =
        NdberSeaiIePdfParser
          .tryParse("", file)
          .get

      assertEquals(Certificate, expectedCertificate)
    }
  }
}
