package ie.nok.ber

import zio.json.{JsonCodec, DeriveJsonCodec}

case class AssessorNumber(value: Int)

object AssessorNumber {
  given JsonCodec[AssessorNumber] = DeriveJsonCodec.gen[AssessorNumber]
}
