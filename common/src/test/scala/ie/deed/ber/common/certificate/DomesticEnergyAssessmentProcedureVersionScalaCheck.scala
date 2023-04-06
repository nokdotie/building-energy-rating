package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.DomesticEnergyAssessmentProcedureVersion
val genDomesticEnergyAssessmentProcedureVersion
    : Gen[DomesticEnergyAssessmentProcedureVersion] =
  Gen.oneOf(DomesticEnergyAssessmentProcedureVersion.values.toSeq)

implicit val arbDomesticEnergyAssessmentProcedureVersion
    : Arbitrary[DomesticEnergyAssessmentProcedureVersion] =
  Arbitrary(genDomesticEnergyAssessmentProcedureVersion)
