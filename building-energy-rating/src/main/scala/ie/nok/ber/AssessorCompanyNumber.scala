package ie.nok.ber

import zio.json.{JsonCodec, DeriveJsonCodec}

case class AssessorCompanyNumber(value: Int)

object AssessorCompanyNumber {
  given JsonCodec[AssessorCompanyNumber] =
    DeriveJsonCodec.gen[AssessorCompanyNumber]
}
