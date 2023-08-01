package ie.nok.ber.common.utils

import scala.util.Try
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.util.chaining.scalaUtilChainingOps

object MapUtils {

  extension (map: java.util.Map[String, Any]) {
    def getNested[A](keys: String*): Try[A] = Try {
      keys
        .foldLeft(map: Any) { (map, key) =>
          map
            .asInstanceOf[java.util.Map[String, Any]]
            .asScala(key)
        }
        .asInstanceOf[A]
        .pipe {
          case null  => throw NullPointerException()
          case other => other
        }
    }

    def getTyped[A](key: String): Try[A] = Try {
      map.get(key).asInstanceOf[A]
    }
  }
}
