package ie.deed.ber.common.java.time

import java.time.Year
import org.scalacheck.{Arbitrary, Gen}

val genYear: Gen[Year] =
  Gen
    .choose(
      min = Year.MIN_VALUE,
      max = Year.MAX_VALUE
    )
    .map(Year.of)

implicit val arbYear: Arbitrary[Year] =
  Arbitrary(genYear)
