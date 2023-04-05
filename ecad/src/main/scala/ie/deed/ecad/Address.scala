package ie.deed.ecad

import scala.util.chaining.scalaUtilChainingOps

sealed abstract case class Address private (value: String)
object Address {
  def apply(value: String): Address =
    value.trim
      .pipe { new Address(_) {} }
}
