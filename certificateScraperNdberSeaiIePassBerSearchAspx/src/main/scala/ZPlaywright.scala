import com.microsoft.playwright._
import java.io.Closeable
import zio._

trait ZPlaywright {
  val acquireRelease: ZIO[Scope, Throwable, Page]
}

object ZPlaywright {

  val acquireRelease: ZIO[ZPlaywright with Scope, Throwable, Page] =
    ZIO.serviceWithZIO[ZPlaywright] { _.acquireRelease }

  def live: ZLayer[Any, Throwable, ZPlaywright] = ZLayer.succeed {
    val acquirePlaywright = ZIO.attempt { Playwright.create }
    val acquireBrowser =
      (playwright: Playwright) => ZIO.attempt { playwright.chromium.launch }
    val acquireContext =
      (browser: Browser) => ZIO.attempt { browser.newContext }
    val acquirePage =
      (context: BrowserContext) => ZIO.attempt { context.newPage }

    new ZPlaywright {
      val acquireRelease = ZIO
        .fromAutoCloseable(acquirePlaywright)
        .flatMap { playwright =>
          ZIO.fromAutoCloseable(acquireBrowser(playwright))
        }
        .flatMap { browser =>
          ZIO.fromAutoCloseable(acquireContext(browser))
        }
        .flatMap { context =>
          ZIO.fromAutoCloseable(acquirePage(context))
        }
    }
  }
}
