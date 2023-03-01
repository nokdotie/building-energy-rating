import scala.util.chaining.scalaUtilChainingOps
import zio._

val certificateNumber = 100000066

val app: ZIO[Playwright, Throwable, Unit] = for {
  _ <- Playwright.navigate("https://ndber.seai.ie/PASS/ber/search.aspx")
  _ <- Playwright.fill(
    "#ctl00_DefaultContent_BERSearch_dfSearch_txtBERNumber",
    certificateNumber.toString
  )
  _ <- Playwright.click("#ctl00_DefaultContent_BERSearch_dfSearch_Bottomsearch")
  _ <- Playwright.click(
    "#ctl00_DefaultContent_BERSearch_gridRatings_gridview_ctl02_ViewDetails"
  )
  values <- List(
    (
      "PublishingAddress",
      "#ctl00_DefaultContent_BERSearch_dfBER_div_PublishingAddress"
    ),
    ("EnergyRating", "#ctl00_DefaultContent_BERSearch_dfBER_div_EnergyRating"),
    ("CDERValue", "#ctl00_DefaultContent_BERSearch_dfBER_div_CDERValue"),
    (
      "DwellingType",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_DwellingType div"
    ),
    (
      "DateOfIssue",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_DateOfIssue div"
    ),
    (
      "DateValidUntil",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_DateValidUntil div"
    ),
    (
      "BERNumber",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_BERNumber div"
    ),
    ("MPRN", "#ctl00_DefaultContent_BERSearch_dfBER_container_MPRN div"),
    (
      "DateOfConstruction",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_DateOfConstruction div"
    ),
    (
      "TypeOfRating",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_TypeOfRating div"
    ),
    ("BERTool", "#ctl00_DefaultContent_BERSearch_dfBER_container_BERTool div"),
    (
      "FloorArea",
      "#ctl00_DefaultContent_BERSearch_dfBER_container_FloorArea div"
    )
  )
    .map { (label, selector) =>
      Playwright.innerHTML(selector).map { (label, _) }
    }
    .pipe { ZIO.collectAll }
  _ <- values
    .map { (label, innerHtml) =>
      val content = innerHtml
        .replaceAll("<br>", "\n")
        .replaceAll("<sup>", "")
        .replaceAll("</sup>", "")
        .replaceAll("<sub>", "")
        .replaceAll("</sub>", "")
        .replaceAll("&nbsp;", " ")
        .replaceAll(" +", " ")
        .trim

      Console.printLine(s"$label: ${content}")
    }
    .pipe { ZIO.collectAll }
} yield ()

object Main extends ZIOAppDefault {
  def run = app.provide(Playwright.live, Scope.default)
}
