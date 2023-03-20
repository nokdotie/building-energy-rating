package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genKilogramOfCarbonDioxidePerSquareMetrePerYear
    : Gen[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }

implicit val arbKilogramOfCarbonDioxidePerSquareMetrePerYear
    : Arbitrary[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Arbitrary(genKilogramOfCarbonDioxidePerSquareMetrePerYear)
