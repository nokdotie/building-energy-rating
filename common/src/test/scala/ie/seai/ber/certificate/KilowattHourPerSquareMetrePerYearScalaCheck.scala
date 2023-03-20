package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genKilowattHourPerSquareMetrePerYear
    : Gen[KilowattHourPerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilowattHourPerSquareMetrePerYear.apply }

implicit val arbKilowattHourPerSquareMetrePerYear
    : Arbitrary[KilowattHourPerSquareMetrePerYear] =
  Arbitrary(genKilowattHourPerSquareMetrePerYear)
