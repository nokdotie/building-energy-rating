package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genKilogramOfCarbonDioxidePerSquareMetrePerYear: Gen[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }

given Arbitrary[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Arbitrary(genKilogramOfCarbonDioxidePerSquareMetrePerYear)
