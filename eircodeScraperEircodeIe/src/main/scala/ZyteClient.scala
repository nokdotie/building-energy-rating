import java.util.Base64
import scala.util.chaining.scalaUtilChainingOps
import zio.json._
import zio.http.{Body, Client}
import zio.http.model.{Headers, Method}
import zio.{durationInt, Schedule, ZIO}

case class ZyteResponseOk(
    httpResponseBody: String
)
object ZyteResponseOk {
  implicit val decoder: JsonDecoder[ZyteResponseOk] =
    DeriveJsonDecoder.gen[ZyteResponseOk]
}

case class ZyteResponseError(
    status: Int,
    title: String,
    detail: String
) extends Throwable(s"Zyte failed: $status, $title, $detail")
object ZyteResponseError {
  implicit val decoder: JsonDecoder[ZyteResponseError] =
    DeriveJsonDecoder.gen[ZyteResponseError]
}

object ZyteClient {
  val headers =
    Headers(
      "Authorization",
      "Basic OGUxNTVhYjVjOWMzNDg3YWJkNDkxYWY5ODM3Mjg4MGQ6"
    ) ++
      Headers("Content-Type", "application/json")

  def getBody(url: String): ZIO[Client, Throwable, String] = {
    val content =
      Body.fromString(s"""{"url": "$url", "httpResponseBody": true}""")

    val requestAndParse = Client
      .request(
        "https://api.zyte.com/v1/extract",
        Method.POST,
        headers,
        content = content
      )
      .flatMap { _.body.asString }
      .flatMap { body =>
        val success = body.fromJson[ZyteResponseOk]
        val error = body.fromJson[ZyteResponseError]

        (success, error) match {
          case (Right(success), _) => ZIO.succeed(success)
          case (_, Right(error))   => ZIO.fail(error)
          case _ => ZIO.fail(Throwable(s"Failed to parse response: $body"))
        }
      }

    requestAndParse
      .retry(
        Schedule.fixed(1.second) &&
          Schedule.recurWhile {
            case ZyteResponseError(429 | 503 | 520, _, _) => true
            case _                                        => false
          }
      )
      .map { _.httpResponseBody }
      .mapAttempt { body =>
        Base64.getDecoder().decode(body).pipe { String(_) }
      }
  }
}
