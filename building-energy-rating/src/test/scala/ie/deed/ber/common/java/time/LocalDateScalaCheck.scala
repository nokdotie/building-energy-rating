package ie.nok.ber.common.java.time

import java.time.LocalDate
import org.scalacheck.{Arbitrary, Gen}

val genLocalDate: Gen[LocalDate] =
  Gen
    .choose(
      min = LocalDate.MIN.toEpochDay,
      max = LocalDate.MAX.toEpochDay
    )
    .map(LocalDate.ofEpochDay)

implicit val arbLocalDate: Arbitrary[LocalDate] =
  Arbitrary(genLocalDate)
