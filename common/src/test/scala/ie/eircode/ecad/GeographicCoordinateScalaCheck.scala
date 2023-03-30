package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

val genGeographicCoordinate: Gen[GeographicCoordinate] = for {
  latitude <- arbitrary[BigDecimal].map { Latitude.apply }
  longitude <- arbitrary[BigDecimal].map { Longitude.apply }
} yield GeographicCoordinate(latitude, longitude)

implicit val arbGeographicCoordinate: Arbitrary[GeographicCoordinate] =
  Arbitrary(genGeographicCoordinate)
