import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}
import scala.language.postfixOps
import scala.sys.process._

object Environment {

  val gitShortSha1: String = ("git rev-parse --short HEAD" !!).trim()

  val instant: String = DateTimeFormatter
    .ofPattern("yyyyMMddHHmmss")
    .withZone(ZoneOffset.UTC)
    .format(Instant.now())

}
