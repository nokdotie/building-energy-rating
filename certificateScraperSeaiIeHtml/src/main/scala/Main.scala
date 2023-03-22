import com.microsoft.playwright.Page
import ie.deed.ber.common.certificate.{
  Certificate,
  CertificateNumber,
  CertificateStore,
  GoogleFirestoreCertificateStore
}
import ie.seai.ber.certificate.HtmlCertificate
import scala.util.chaining.scalaUtilChainingOps
import zio.{Scope, ZIO, ZIOAppDefault}
import zio.gcp.firestore.Firestore
import zio.stream.ZPipeline

def getHtmlCertificate(
    certificateNumber: CertificateNumber
): ZIO[ZPlaywright with Scope, Throwable, HtmlCertificate] = {
  val timeoutInMilliseconds = 3000

  for {
    page <- ZPlaywright.acquireRelease
    _ <- ZIO.attemptBlocking {
      page.navigate("https://ndber.seai.ie/PASS/BER/Search.aspx")

      page.fill(
        s"#ctl00_DefaultContent_BERSearch_dfSearch_txtBERNumber",
        certificateNumber.value.toString,
        Page.FillOptions().setTimeout(timeoutInMilliseconds)
      )
      page.click(
        s"#ctl00_DefaultContent_BERSearch_dfSearch_Bottomsearch",
        Page.ClickOptions().setTimeout(timeoutInMilliseconds)
      )

      page.click(
        s"#ctl00_DefaultContent_BERSearch_gridRatings_gridview_ctl02_ViewDetails",
        Page.ClickOptions().setTimeout(timeoutInMilliseconds)
      )
    }
    certificate <- ZIO
      .fromTry { PageParser.tryParse(page) }
      .logError("HTML Parser Failed")
  } yield certificate
}

val getCertificates: ZPipeline[
  ZPlaywright with Scope,
  Throwable,
  CertificateNumber,
  Certificate
] = {
  val concurrency = 10

  ZPipeline[CertificateNumber]
    .mapZIOParUnordered(concurrency) { certificateNumber =>
      getHtmlCertificate(certificateNumber)
        .retryN(3)
        .map { htmlCertificate =>
          Certificate(
            certificateNumber,
            Some(htmlCertificate),
            None
          )
        }
        .option
    }
    .collectSome
}

val upsertLimit = 1_000

val app: ZIO[CertificateStore with ZPlaywright with Scope, Throwable, Unit] =
  CertificateStore.streamMissingSeaiIeHtml
    .debug("Certificate Number")
    .via(getCertificates)
    .debug("Certificate")
    .via(CertificateStore.upsertPipeline)
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
