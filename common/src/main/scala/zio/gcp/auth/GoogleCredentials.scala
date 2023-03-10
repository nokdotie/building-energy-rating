package zio.gcp.auth

import com.google.auth.oauth2.{GoogleCredentials => OfficialGoogleCredentials}
import java.io.{ByteArrayInputStream, IOException}
import scala.util.chaining.scalaUtilChainingOps
import zio.ZIO

object GoogleCredentials {
  private val officialApplicationDefault
      : ZIO[Any, Throwable, OfficialGoogleCredentials] =
    ZIO.attempt { OfficialGoogleCredentials.getApplicationDefault() }

  // TODO: properly authenticate on GitHub Action
  private val hack: ZIO[Any, Throwable, OfficialGoogleCredentials] =
    ZIO.attempt {
      sys
        .env("GCP_CREDENTIALS")
        .getBytes
        .pipe { ByteArrayInputStream(_) }
        .pipe { OfficialGoogleCredentials.fromStream }
    }

  val applicationDefault: ZIO[Any, Throwable, OfficialGoogleCredentials] =
    hack.orElse(officialApplicationDefault)
}
