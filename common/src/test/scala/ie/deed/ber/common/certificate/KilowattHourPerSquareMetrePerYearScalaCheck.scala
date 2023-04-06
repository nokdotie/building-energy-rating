package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.KilowattHourPerSquareMetrePerYear
val genKilowattHourPerSquareMetrePerYear
    : Gen[KilowattHourPerSquareMetrePerYear] =
  Gen.posNum[Float].map { KilowattHourPerSquareMetrePerYear.apply }

implicit val arbKilowattHourPerSquareMetrePerYear
    : Arbitrary[KilowattHourPerSquareMetrePerYear] =
  Arbitrary(genKilowattHourPerSquareMetrePerYear)
