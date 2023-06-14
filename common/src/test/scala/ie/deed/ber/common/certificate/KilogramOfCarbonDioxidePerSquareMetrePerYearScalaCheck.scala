package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.KilogramOfCarbonDioxidePerSquareMetrePerYear
val genKilogramOfCarbonDioxidePerSquareMetrePerYear
    : Gen[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }

implicit val arbKilogramOfCarbonDioxidePerSquareMetrePerYear
    : Arbitrary[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
  Arbitrary(genKilogramOfCarbonDioxidePerSquareMetrePerYear)
