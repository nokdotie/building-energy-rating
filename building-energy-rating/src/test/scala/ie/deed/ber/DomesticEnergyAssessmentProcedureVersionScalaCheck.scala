package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genDomesticEnergyAssessmentProcedureVersion
    : Gen[DomesticEnergyAssessmentProcedureVersion] =
  Gen.oneOf(DomesticEnergyAssessmentProcedureVersion.values.toSeq)

given Arbitrary[DomesticEnergyAssessmentProcedureVersion] =
  Arbitrary(genDomesticEnergyAssessmentProcedureVersion)
