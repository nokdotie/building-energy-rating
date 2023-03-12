package ie.deed.ber.common.dao

import ie.deed.ber.common.model.*
import ie.deed.ber.common.model.BERClass.*
import ie.deed.ber.common.model.DwellingType.*
import ie.deed.ber.common.model.TypeOfRating.*

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BERRecordInMemoryDao extends BERRecordDao {

  override def getByBerNumber(number: Int): Option[BERRecord] =
    recordsByBerNumber.get(number)

  override def getByEirCode(eirCode: String): Option[BERRecord] =
    recordsByEirCode.get(eirCode)

  private lazy val recordsByBerNumber: Map[Int, BERRecord] =
    records.groupMapReduce(_.number)(record => record)((a, _) => a)

  private lazy val recordsByEirCode: Map[String, BERRecord] =
    records.flatMap(record => record.address.eirCode.map((_, record))).toMap

  private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyy")

  private val records = List(
    BERRecord(
      number = 100469675,
      MPRN = None,
      dateOfIssue = LocalDate.parse("20-08-2022", formatter),
      dateValidUntil = LocalDate.parse("20-08-2032", formatter),
      ber = BER(C1, 160.41),
      co2EmissionIndicator = CO2EmissionIndicator(39.72),
      typeOfRating = ExistingDwelling,
      DEAPVersion = "4.1.0",
      address = Address(
        "33 HAZELWOOD\nCOSMONA\nLOUGHREA\nCO. GALWAY",
        eirCode = Some("H62XE37")
      ),
      dwellingType = SemiDetachedHouse,
      floorArea = FloorArea(116.26),
      yearOfConstruction = 2002,
      certificate = BERCertificate(
        certificateUrl =
          "https://ndber.seai.ie/pass/Download/PassDownloadBER.ashx?type=nas&ber=100469675&file=bercert",
        advisoryReportUrl = Option(
          "https://ndber.seai.ie/pass/Download/PassDownloadBER.ashx?type=nas&ber=100469675&file=advisoryreport"
        )
      )
    ),
    BERRecord(
      number = 100469683,
      MPRN = None,
      dateOfIssue = LocalDate.parse("23-02-2022", formatter),
      dateValidUntil = LocalDate.parse("23-02-2032", formatter),
      ber = BER(D1, 229.51),
      co2EmissionIndicator = CO2EmissionIndicator(49.52),
      typeOfRating = ExistingDwelling,
      DEAPVersion = "4.0.0",
      address = Address("BALLYKILMURRAY\nTULLAMORE\nCO. OFFALY"),
      dwellingType = DetachedHouse,
      floorArea = FloorArea(176.5),
      yearOfConstruction = 2005,
      certificate = BERCertificate(
        certificateUrl =
          "https://ndber.seai.ie/pass/Download/PassDownloadBER.ashx?type=nas&ber=100469683&file=bercert",
        advisoryReportUrl = Option(
          "https://ndber.seai.ie/pass/Download/PassDownloadBER.ashx?type=nas&ber=100469683&file=advisoryreport"
        )
      )
    ),
    BERRecord(
      number = 100469758,
      MPRN = None,
      dateOfIssue = LocalDate.parse("20-05-2013", formatter),
      dateValidUntil = LocalDate.parse("20-05-2023", formatter),
      ber = BER(E2, 379.87),
      co2EmissionIndicator = CO2EmissionIndicator(98.79),
      typeOfRating = ExistingDwelling,
      DEAPVersion = "3.2.1",
      address = Address("GORTBAWN\nSUMMERVILLE AVENUE\nWATERFORD CITY"),
      dwellingType = DetachedHouse,
      floorArea = FloorArea(119.06),
      yearOfConstruction = 1976,
      certificate = BERCertificate(
        certificateUrl =
          "https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&ber=100469758&file=bercert",
        advisoryReportUrl = None
      )
    )
  )

}
