import com.microsoft.playwright.{Page, Playwright}
import java.io.File
import java.time.{LocalDate, Year}
import ie.seai.ber.certificate._
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

class PageParserSuite extends munit.FunSuite {
  val htmlPathAndExpectedHtmlCertificate = List(
    (
      "./certificates/100000066.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2018, 9, 2),
        LocalDate.of(2028, 9, 2),
        Address(
          "5 LAKESIDE\nOLDWOOD\nGOLFLINKS ROAD\nROSCOMMON\nCO. ROSCOMMON"
        ),
        Year.of(2007),
        PropertyType.DetachedHouse,
        SquareMeter(219.13),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(139.01),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(27.93)
      )
    ),
    (
      "./certificates/100000181.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2019, 6, 3),
        LocalDate.of(2029, 6, 3),
        Address("2 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        Year.of(2007),
        PropertyType.DetachedHouse,
        SquareMeter(318.25),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(133.33),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(33.65)
      )
    ),
    (
      "./certificates/100000280.html",
      HtmlCertificate(
        Rating.B2,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2016, 4, 25),
        LocalDate.of(2026, 4, 25),
        Address("8 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        Year.of(2007),
        PropertyType.DetachedHouse,
        SquareMeter(251.34),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(123.36),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(29.2)
      )
    ),
    (
      "./certificates/100000298.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2020, 8, 15),
        LocalDate.of(2030, 8, 15),
        Address("9 CURRAGH WOOD\nKILANERIN\nGOREY\nCO. WEXFORD"),
        Year.of(2007),
        PropertyType.DetachedHouse,
        SquareMeter(222.72),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(141.54),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(31.72)
      )
    ),
    (
      "./certificates/100000595.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2022, 1, 20),
        LocalDate.of(2032, 1, 20),
        Address("9 MIMOSA HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"),
        Year.of(2006),
        PropertyType.MidFloorApartment,
        SquareMeter(89.7),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(126.6),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(23.49)
      )
    ),
    (
      "./certificates/100000652.html",
      HtmlCertificate(
        Rating.B2,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2021, 10, 18),
        LocalDate.of(2031, 10, 18),
        Address("APT 13 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"),
        Year.of(2007),
        PropertyType.TopFloorApartment,
        SquareMeter(83.7),
        DomesticEnergyAssessmentProcedureVersion.`4.0.0`,
        KilowattHourPerSquareMetrePerYear(112.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(20.8)
      )
    ),
    (
      "./certificates/100000645.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2022, 9, 23),
        LocalDate.of(2032, 9, 23),
        Address("APT 12 MINOSA HALL\nTHE GALLOPS\nLEOPARDSTOWN\nDUBLIN 18"),
        Year.of(2006),
        PropertyType.TopFloorApartment,
        SquareMeter(98.45),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(143.07),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.54)
      )
    ),
    (
      "./certificates/100000744.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2023, 3, 9),
        LocalDate.of(2033, 3, 9),
        Address(
          "APARTMENT 22 LEVMOSS HALL\nLEVMOSS PARK\nTHE GALLOPS\nDUBLIN 18"
        ),
        Year.of(2006),
        PropertyType.GroundFloorApartment,
        SquareMeter(85.52),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(145.67),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(26.44)
      )
    ),
    (
      "./certificates/100000967.html",
      HtmlCertificate(
        Rating.B3,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2022, 5, 25),
        LocalDate.of(2032, 5, 25),
        Address("APT 16 THE GALLOPS\nLEVMOSS HALL\nLEOPARDSTOWN\nDUBLIN 18"),
        Year.of(2007),
        PropertyType.TopFloorApartment,
        SquareMeter(84.4),
        DomesticEnergyAssessmentProcedureVersion.`4.1.0`,
        KilowattHourPerSquareMetrePerYear(148.69),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(27.55)
      )
    ),
    (
      "./certificates/106559123.html",
      HtmlCertificate(
        Rating.F,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2014, 6, 28),
        LocalDate.of(2024, 6, 28),
        Address("RHINE\nKILLOE\nCO. LONGFORD"),
        Year.of(2000),
        PropertyType.DetachedHouse,
        SquareMeter(96.4),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(442.7),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(143.21)
      )
    ),
    (
      "./certificates/106559149.html",
      HtmlCertificate(
        Rating.G,
        TypeOfRating.ExistingDwelling,
        LocalDate.of(2014, 6, 29),
        LocalDate.of(2024, 6, 29),
        Address("303 CONNOLLY ROAD\nPORTLAW\nCO. WATERFORD"),
        Year.of(1940),
        PropertyType.MidTerraceHouse,
        SquareMeter(76.03),
        DomesticEnergyAssessmentProcedureVersion.`3.2.1`,
        KilowattHourPerSquareMetrePerYear(451.65),
        KilogramOfCarbonDioxidePerSquareMetrePerYear(140.69)
      )
    )
  )

  test("should parse all certificates correctly") {
    Using.resource(Playwright.create) { playwright =>
      Using.resource(playwright.chromium.launch) { browser =>
        Using.resource(browser.newPage) { page =>
          htmlPathAndExpectedHtmlCertificate.foreach {
            (htmlPath, expectedHtmlCertificate) =>
              val file = getClass()
                .getClassLoader()
                .getResource(htmlPath)
                .getFile()

              page.navigate(s"file://$file")

              val htmlCertificate = PageParser.tryParse(page).get

              assertEquals(htmlCertificate, expectedHtmlCertificate)
          }
        }
      }
    }
  }
}
