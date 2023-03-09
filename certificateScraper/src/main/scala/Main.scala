import com.microsoft.playwright._
import ie.deed.ber.common.certificate.CertificateNumber
import scala.util.chaining.scalaUtilChainingOps
import zio._
import zio.stream._

val certificateNumbers: ZStream[Any, Nothing, CertificateNumber] =
  ZStream(
    100000066, 100000181, 100000280, 100000298, 100000389, 100000405, 100000421,
    100000553, 100000561, 100000595, 100000603, 100000645, 100000652, 100000660,
    100000678, 100000686, 100000728, 100000736, 100000777, 100000793, 100000801,
    100000819, 100000827, 100000835, 100000876, 100000918, 100000959, 100000967,
    100000975, 100001031, 100001056, 100001072, 100001080, 100001114, 100001148,
    100001197, 100001221, 100001239, 100001254, 100001296, 100001882, 100001908,
    100001916, 100002443, 100002476, 100002526, 100002542, 100002559, 100002575,
    100002583, 100002609, 100002617, 100002658, 100002708, 100002740, 100002765,
    100002773, 100002807, 100002823, 100002880, 100002898, 100002922, 100003011,
    100003029, 100003045, 100003052, 100003060, 100003078, 100003094, 100003342,
    100003391, 100003813, 100004001, 100004043, 100004209, 100004233, 100004241,
    100004258, 100004506, 100004530, 100004886, 100005032, 100005073, 100005123,
    100005149, 100005164, 100005552, 100005586, 100005925, 100005933, 100006014,
    100006105, 100006121, 100006147, 100006170, 100006204, 100006238, 100006246,
    100006352, 100006436
  )
    .map { CertificateNumber.apply }

val idSelectorPrefix = "#ctl00_DefaultContent_BERSearch"
def getFieldValue(page: Page, fieldName: String): String = {
  val selector =
    s"${idSelectorPrefix}_dfBER_container_${fieldName} div"

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

val seaiDateTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

val getCertificate: ZPipeline[
  ZPlaywright with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 5

  ZPipeline[CertificateNumber]
    // .mapZIOParUnordered(concurrency) { certificateNumber =>
    .mapZIO { certificateNumber =>
      ZPlaywright.acquireRelease.flatMap { page =>
        ZIO
          .attemptBlocking {
            page.navigate("https://ndber.seai.ie/PASS/BER/Search.aspx")

            page.waitForTimeout(1000)

            val captcha = page.inputValue(s"${idSelectorPrefix}_captcha")
            println(s"Captcha: $captcha")

            page.fill(
              s"${idSelectorPrefix}_dfSearch_txtBERNumber",
              certificateNumber.value.toString
            )
            page.click(s"${idSelectorPrefix}_dfSearch_Bottomsearch")

            page.click(
              s"${idSelectorPrefix}_gridRatings_gridview_ctl02_ViewDetails"
            )

            val typeOfRating = getFieldValue(page, "TypeOfRating").pipe {
              TypeOfRating.tryFromString
            }.get

            val issuedOn = getFieldValue(page, "DateOfIssue")
              .pipe { LocalDate.parse(_, seaiDateTimeFormat) }
            val validUntil = getFieldValue(page, "DateValidUntil")
              .pipe { LocalDate.parse(_, seaiDateTimeFormat) }

            val propertyMeterPointReferenceNumber =
              getFieldValue(page, "MPRN")
                .pipe { _.toIntOption }
                .map { MeterPointReferenceNumber.apply }

            val address = getFieldValue(page, "PublishingAddress")
              .pipe { Address.apply }

            val propertyConstructedOn =
              getFieldValue(page, "DateOfConstruction")
                .pipe { Year.parse }

            val propertyType = getFieldValue(page, "DwellingType").pipe {
              PropertyType.tryFromString
            }.get

            val propertyFloorArea = getFieldValue(page, "FloorArea").pipe {
              SquareMeter.tryFromString
            }.get

            val domesticEnergyAssessmentProcedureVersion =
              getFieldValue(page, "BERTool").pipe {
                DomesticEnergyAssessmentProcedureVersion.tryFromString
              }.get

            val energyRating = getFieldValue(page, "EnergyRating").pipe {
              KilowattHourPerSquareMetrePerYear.tryFromString
            }.get

            val carbonDioxideEmissionsIndicator =
              getFieldValue(page, "CDERValue").pipe {
                KilogramOfCarbonDioxidePerSquareMetrePerYear.tryFromString
              }.get

            Certificate(
              certificateNumber,
              typeOfRating,
              issuedOn,
              validUntil,
              propertyMeterPointReferenceNumber,
              address,
              propertyConstructedOn,
              propertyType,
              propertyFloorArea,
              domesticEnergyAssessmentProcedureVersion,
              energyRating,
              carbonDioxideEmissionsIndicator
            )
          }
      }
    }
}

val app: ZIO[ZPlaywright with Scope, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(getCertificate)
    .debug("Certificate")
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    ZPlaywright.live,
    Scope.default
  )
}
