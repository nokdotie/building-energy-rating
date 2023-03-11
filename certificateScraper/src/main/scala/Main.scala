import com.microsoft.playwright._
import ie.deed.ber.common.certificate.{
  Certificate,
  CertificateNumber,
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import ie.deed.ber.common.certificate.seaiie.{
  Certificate => SeaiIeCertificate,
  _
}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, Year}
import scala.util.chaining.scalaUtilChainingOps
import zio._
import zio.gcp.firestore.Firestore
import zio.stream._

val certificateNumbers
    : ZStream[CertificateStore, Throwable, CertificateNumber] =
  CertificateStore.streamMissingSeaiIe

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
              Some(
                SeaiIeCertificate(
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
              )
            )
          }
      }
    }
}

val upsert: ZPipeline[CertificateStore, Throwable, Certificate, Int] =
  ZPipeline
    .chunks[Certificate]
    .mapZIO { chunks => CertificateStore.upsertBatch(chunks.toList) }
    .andThen { ZPipeline.fromFunction { _.scan(0) { _ + _ } } }

val upsertLimit = 1_000

val app: ZIO[CertificateStore with ZPlaywright with Scope, Throwable, Unit] =
  certificateNumbers
    .debug("Certificate Number")
    .via(getCertificate)
    .debug("Certificate")
    .via(upsert)
    .debug("Certificate Number Upserted")
    .takeWhile { _ < upsertLimit }
    .runDrain

object Main extends ZIOAppDefault {
  def run = app.provide(
    Firestore.live,
    GoogleFirestoreCertificateStore.layer,
    Scope.default,
    ZPlaywright.live
  )
}
