import com.microsoft.playwright.{Playwright => MsPlaywright, _}
import java.io.Closeable
import zio._

trait Playwright {
  def navigate(url: String): ZIO[Any, Throwable, Unit]

  def fill(selector: String, value: String): ZIO[Any, Throwable, Unit]
  def click(selector: String): ZIO[Any, Throwable, Unit]
  def innerHTML(selector: String): ZIO[Any, Throwable, String]
}

object Playwright {

  def navigate(url: String): ZIO[Playwright, Throwable, Unit] =
    ZIO.serviceWithZIO[Playwright](
      _.navigate(url)
    )

  def fill(selector: String, value: String): ZIO[Playwright, Throwable, Unit] =
    ZIO.serviceWithZIO[Playwright](
      _.fill(selector, value)
    )
  def click(selector: String): ZIO[Playwright, Throwable, Unit] =
    ZIO.serviceWithZIO[Playwright](
      _.click(selector)
    )
  def innerHTML(selector: String): ZIO[Playwright, Throwable, String] =
    ZIO.serviceWithZIO[Playwright](
      _.innerHTML(selector)
    )

  def live: ZLayer[Scope, Throwable, Playwright] = ZLayer.fromZIO {
    val acquirePlaywright = ZIO.attemptBlocking { MsPlaywright.create }
    val acquireBrowser = (playwright: MsPlaywright) =>
      ZIO.attemptBlocking { playwright.chromium.launch }
    val acquirePage =
      (browser: Browser) => ZIO.attemptBlocking { browser.newPage }

    ZIO
      .fromAutoCloseable(acquirePlaywright)
      .flatMap { playwright =>
        ZIO.fromAutoCloseable(acquireBrowser(playwright))
      }
      .flatMap { browser =>
        ZIO.fromAutoCloseable(acquirePage(browser))
      }
      .map { page =>
        new Playwright {
          def navigate(url: String): ZIO[Any, Throwable, Unit] =
            ZIO.attemptBlocking {
              page.navigate(url)
            }

          def fill(selector: String, value: String): ZIO[Any, Throwable, Unit] =
            ZIO.attemptBlocking {
              page.locator(selector).fill(value)
            }

          def click(selector: String): ZIO[Any, Throwable, Unit] =
            ZIO.attemptBlocking {
              page.locator(selector).click()
            }

          def innerHTML(selector: String): ZIO[Any, Throwable, String] =
            ZIO.attemptBlocking {
              page.locator(selector).innerHTML()
            }
        }
      }
  }
}
