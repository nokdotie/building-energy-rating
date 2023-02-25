package ie.deed.ber.common.model

import java.net.URL
import java.time.{LocalDate, Year}
import java.util.Date
import zio.json.{DeriveJsonCodec, DeriveJsonDecoder, DeriveJsonEncoder, JsonCodec, JsonDecoder, JsonEncoder}

enum BERClass {
  case A1, A2, A3, B1, B2, B3, C1, C2, C3, D1, D2, E1, E2, F, G
}
case class BER(berClass: BERClass, primaryEnergyUse: Double)
object BER {
  val unit: String = "kWh/m2/yr"
}

case class CO2EmissionIndicator(value: Double)
object CO2EmissionIndicator {
  val unit: String = "kgCO2/m2/yr"
}

enum TypeOfRating {
  case ExistingDwelling
}

case class Address(raw: String, eirCode: Option[String] = None)

enum DwellingType {
  case DetachedHouse, SemiDetachedHouse
}

case class FloorArea(valueSqm: Double) {
  val valueSqf: Double = valueSqm * FloorArea.sqmToSqf
}

object FloorArea {
  val unit: String = "m2" // or sqm
  private val sqmToSqf: Double = 10.763914692
}

case class BERCertificate(certificateUrl: String, advisoryReportUrl: Option[String])

case class BERRecord(
    number: Int,
    MPRN: Option[Int],
    dateOfIssue: LocalDate,
    dateValidUntil: LocalDate,
    ber: BER,
    co2EmissionIndicator: CO2EmissionIndicator,
    typeOfRating: TypeOfRating,
    DEAPVersion: String, // could be enum; Domestic Energy Assessment Procedure
    address: Address,
    dwellingType: DwellingType,
    floorArea: FloorArea,
    yearOfConstruction: Int,
    certificate: BERCertificate
)
