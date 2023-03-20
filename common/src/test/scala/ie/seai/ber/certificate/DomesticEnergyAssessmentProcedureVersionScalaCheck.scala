package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genDomesticEnergyAssessmentProcedureVersion
    : Gen[DomesticEnergyAssessmentProcedureVersion] =
  Gen.oneOf(DomesticEnergyAssessmentProcedureVersion.values.toSeq)

implicit val arbDomesticEnergyAssessmentProcedureVersion
    : Arbitrary[DomesticEnergyAssessmentProcedureVersion] =
  Arbitrary(genDomesticEnergyAssessmentProcedureVersion)
