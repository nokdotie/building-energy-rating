import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}
import scala.sys.process._

object Environment {

  val gitShortSha1 = ("git rev-parse --short HEAD" !!).trim()

  val instant = DateTimeFormatter
    .ofPattern("yyyyMMddHHmmss")
    .withZone(ZoneOffset.UTC)
    .format(Instant.now())

}
