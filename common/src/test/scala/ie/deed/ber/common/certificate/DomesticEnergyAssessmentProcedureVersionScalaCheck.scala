package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.DomesticEnergyAssessmentProcedureVersion
val genDomesticEnergyAssessmentProcedureVersion
    : Gen[DomesticEnergyAssessmentProcedureVersion] =
  Gen.oneOf(DomesticEnergyAssessmentProcedureVersion.values.toSeq)

implicit val arbDomesticEnergyAssessmentProcedureVersion
    : Arbitrary[DomesticEnergyAssessmentProcedureVersion] =
  Arbitrary(genDomesticEnergyAssessmentProcedureVersion)
