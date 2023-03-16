package ie.seai.ber.certificate

import java.time.{LocalDate, Year}

case class HtmlCertificate(
    typeOfRating: TypeOfRating,
    issuedOn: LocalDate,
    validUntil: LocalDate,
    propertyAddress: Address,
    propertyConstructedOn: Year,
    propertyType: PropertyType,
    propertyFloorArea: SquareMeter,
    domesticEnergyAssessmentProcedureVersion: DomesticEnergyAssessmentProcedureVersion,
    energyRating: KilowattHourPerSquareMetrePerYear,
    carbonDioxideEmissionsIndicator: KilogramOfCarbonDioxidePerSquareMetrePerYear
)
