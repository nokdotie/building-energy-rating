package ie.nok.ber

import scala.util.{Try, Success, Failure}
import zio.json.{JsonCodec, DeriveJsonCodec}

enum DomesticEnergyAssessmentProcedureVersion {
  case `3.2.1`
  case `4.0.0`, `4.1.0`
}

object DomesticEnergyAssessmentProcedureVersion {
  import DomesticEnergyAssessmentProcedureVersion._

  def tryFromString(
      value: String
  ): Try[DomesticEnergyAssessmentProcedureVersion] = value match {
    case "3.2.1" => Success(`3.2.1`)
    case "4.0.0" => Success(`4.0.0`)
    case "4.1.0" => Success(`4.1.0`)
    case unknown =>
      Failure(
        Exception(
          s"Unknown domestic energy assessment procedure version: $unknown"
        )
      )
  }

  given JsonCodec[DomesticEnergyAssessmentProcedureVersion] =
    DeriveJsonCodec.gen[DomesticEnergyAssessmentProcedureVersion]
}
