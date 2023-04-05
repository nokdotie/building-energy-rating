package ie.deed.ecad

import scala.util.chaining.scalaUtilChainingOps

sealed abstract case class Eircode private (value: String)
object Eircode {
  def apply(value: String): Eircode =
    value.toUpperCase
      .filter { _.isLetterOrDigit }
      .pipe { new Eircode(_) {} }
}
