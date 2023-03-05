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

val getCertificate: ZPipeline[
  ZPlaywright with Scope,
  Throwable,
  CertificateNumber,
  (
      String,
      String,
      String,
      String,
      String,
      String,
      String,
      String,
      String,
      String,
      String,
      String
  )
] = {
  val concurrency = 1

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      ZPlaywright.acquireRelease.flatMap { page =>
        ZIO.attemptBlocking {
          page.navigate("https://ndber.seai.ie/PASS/ber/search.aspx")

          page
            .locator(s"${idSelectorPrefix}_dfSearch_Bottomsearch")
            .click()

          page
            .locator(s"${idSelectorPrefix}_dfSearch_txtBERNumber")
            .fill(certificateNumber.value.toString)

          page
            .locator(s"${idSelectorPrefix}_dfSearch_Bottomsearch")
            .click()

          page
            .locator(
              s"${idSelectorPrefix}_gridRatings_gridview_ctl02_ViewDetails"
            )
            .click()

          val address = getFieldValue(page, "PublishingAddress")
          val buildingEnergyRating = getFieldValue(page, "EnergyRating")
          val co2EmissionsIndicator = getFieldValue(page, "CDERValue")
          val dwellingType = getFieldValue(page, "DwellingType")
          val dateOfIssue = getFieldValue(page, "DateOfIssue")
          val dateValidUntil = getFieldValue(page, "DateValidUntil")
          val berDecNumber = getFieldValue(page, "BERNumber")
          val mprn = getFieldValue(page, "MPRN")
          val yearOfConstruction = getFieldValue(page, "DateOfConstruction")
          val typeOfRating = getFieldValue(page, "TypeOfRating")
          val deapVersion = getFieldValue(page, "BERTool")
          val floorArea = getFieldValue(page, "FloorArea")

          (
            address,
            buildingEnergyRating,
            co2EmissionsIndicator,
            dwellingType,
            dateOfIssue,
            dateValidUntil,
            berDecNumber,
            mprn,
            yearOfConstruction,
            typeOfRating,
            deapVersion,
            floorArea,
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
  def run = app.provide(ZPlaywright.live, Scope.default)
}
