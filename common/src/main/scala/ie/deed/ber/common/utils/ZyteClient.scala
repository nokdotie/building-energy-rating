package ie.deed.ber.common.utils

import java.util.Base64
import scala.util.chaining.scalaUtilChainingOps
import zio.json.{JsonDecoder, DeriveJsonDecoder, DecoderOps}
import zio.http.{Body, Client, Response}
import zio.http.model.{Headers, Method}
import zio.{durationInt, Schedule, ZIO}
import zio.Schedule.{recurs, exponential}

object ZyteClient {

  case class ZyteResponseOk(httpResponseBody: String)
  given JsonDecoder[ZyteResponseOk] = DeriveJsonDecoder.gen[ZyteResponseOk]

  case class ZyteResponseError(
      status: Int,
      title: String,
      detail: String
  ) extends Throwable(s"Zyte failed: $status, $title, $detail")
  given JsonDecoder[ZyteResponseError] =
    DeriveJsonDecoder.gen[ZyteResponseError]

  val headers: Headers =
    Headers(
      "Authorization",
      "Basic OGUxNTVhYjVjOWMzNDg3YWJkNDkxYWY5ODM3Mjg4MGQ6"
    ) ++
      Headers("Content-Type", "application/json")

  private def zyteResponseOkToResponse(
      zyteResponseOk: ZyteResponseOk
  ): ZIO[Any, Throwable, Response] =
    ZIO
      .attempt {
        zyteResponseOk.httpResponseBody
          .pipe { Base64.getDecoder.decode }
          .pipe { String(_) }
      }
      .map { body => Response(body = Body.fromString(body)) }

  def request(url: String): ZIO[Client, Throwable, Response] = {
    val content =
      Body.fromString(s"""{"url": "$url", "httpResponseBody": true}""")

    val requestAndParse = Client
      .request(
        "https://api.zyte.com/v1/extract",
        Method.POST,
        headers,
        content = content
      )
      .retry(recurs(3) && exponential(10.milliseconds))
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
      .flatMap(zyteResponseOkToResponse)

    requestAndParse
      .retry(
        Schedule.fixed(1.second) &&
          Schedule.recurWhile {
            case ZyteResponseError(429 | 500 | 503 | 520, _, _) => true
            case _                                              => false
          }
      )
  }
}
