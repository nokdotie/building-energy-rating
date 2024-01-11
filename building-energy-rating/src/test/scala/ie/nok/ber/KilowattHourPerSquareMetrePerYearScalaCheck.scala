package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genKilowattHourPerSquareMetrePerYear: Gen[KilowattHourPerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilowattHourPerSquareMetrePerYear.apply }

given Arbitrary[KilowattHourPerSquareMetrePerYear] =
  Arbitrary(genKilowattHourPerSquareMetrePerYear)
