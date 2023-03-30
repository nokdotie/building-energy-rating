package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

val genGeographicCoordinate: Gen[GeographicCoordinate] = for {
  latitude <- Gen.choose[BigDecimal](-90, 90).map { Latitude.apply }
  longitude <- Gen.choose[BigDecimal](-180, 180).map { Longitude.apply }
} yield GeographicCoordinate(latitude, longitude)

implicit val arbGeographicCoordinate: Arbitrary[GeographicCoordinate] =
  Arbitrary(genGeographicCoordinate)
