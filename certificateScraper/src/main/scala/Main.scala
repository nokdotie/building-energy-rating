import com.microsoft.playwright._
import java.nio.file.Paths
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

object Main extends App {
    println("Hello, world")

    val certificateNumber = 100000066
    Using(Playwright.create()) { playwright =>
      Using(playwright.chromium().launch()) { browser =>
        val page = browser.newPage();

        page.navigate("https://ndber.seai.ie/PASS/ber/search.aspx")
        page.locator("#ctl00_DefaultContent_BERSearch_dfSearch_txtBERNumber")
          .fill(certificateNumber.toString())

        page.locator("#ctl00_DefaultContent_BERSearch_dfSearch_Bottomsearch")
          .click()

        page.locator("#ctl00_DefaultContent_BERSearch_gridRatings_gridview_ctl02_ViewDetails")
          .click()

        List(
            ("PublishingAddress", "ctl00_DefaultContent_BERSearch_dfBER_div_PublishingAddress"),
            ("EnergyRating", "ctl00_DefaultContent_BERSearch_dfBER_div_EnergyRating"),
            ("CDERValue", "ctl00_DefaultContent_BERSearch_dfBER_div_CDERValue"),
            ("DwellingType", "ctl00_DefaultContent_BERSearch_dfBER_container_DwellingType div"),
            ("DateOfIssue", "ctl00_DefaultContent_BERSearch_dfBER_container_DateOfIssue div"),
            ("DateValidUntil", "ctl00_DefaultContent_BERSearch_dfBER_container_DateValidUntil div"),
            ("BERNumber", "ctl00_DefaultContent_BERSearch_dfBER_container_BERNumber div"),
            ("MPRN", "ctl00_DefaultContent_BERSearch_dfBER_container_MPRN div"),
            ("DateOfConstruction", "ctl00_DefaultContent_BERSearch_dfBER_container_DateOfConstruction div"),
            ("TypeOfRating", "ctl00_DefaultContent_BERSearch_dfBER_container_TypeOfRating div"),
            ("BERTool", "ctl00_DefaultContent_BERSearch_dfBER_container_BERTool div"),
            ("FloorArea", "ctl00_DefaultContent_BERSearch_dfBER_container_FloorArea div"),
        ).foreach { (label, id) =>
            page.locator(s"#$id")
                .innerHTML()
                .trim
                .replaceAll("<br>", " ")
                .replaceAll("<sup>", "")
                .replaceAll("</sup>", "")
                .replaceAll("<sub>", "")
                .replaceAll("</sub>", "")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .tap { content => println(s"$label: ${content}") }
        }
      }
    }
    .flatten
    .tap { result => println(s"Finished: ${result}") }
}
