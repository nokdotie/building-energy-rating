package ie.deed.ber.api

import ie.deed.ber.common.model.*
import zio.json.{
  DeriveJsonCodec,
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  JsonCodec,
  JsonDecoder,
  JsonEncoder
}

object BERRecordCodecs {
  implicit val codecBERCertificate: JsonCodec[BERCertificate] =
    DeriveJsonCodec.gen[BERCertificate]
  implicit val codecFloorArea: JsonCodec[FloorArea] =
    DeriveJsonCodec.gen[FloorArea]
  implicit val codecDwellingType: JsonCodec[DwellingType] =
    DeriveJsonCodec.gen[DwellingType]
  implicit val codecAddress: JsonCodec[Address] = DeriveJsonCodec.gen[Address]
  implicit val codecTypeOfRating: JsonCodec[TypeOfRating] =
    DeriveJsonCodec.gen[TypeOfRating]
  implicit val codecCO2EmissionIndicator: JsonCodec[CO2EmissionIndicator] =
    DeriveJsonCodec.gen[CO2EmissionIndicator]
  implicit val codecBERClass: JsonCodec[BERClass] =
    DeriveJsonCodec.gen[BERClass]
  implicit val codecBER: JsonCodec[BER] = DeriveJsonCodec.gen[BER]
  implicit val codecBERRecord: JsonCodec[BERRecord] =
    DeriveJsonCodec.gen[BERRecord]
}
