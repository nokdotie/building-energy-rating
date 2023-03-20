package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genPropertyType: Gen[PropertyType] =
  Gen.oneOf(PropertyType.values.toSeq)

implicit val arbPropertyType: Arbitrary[PropertyType] =
  Arbitrary(genPropertyType)
