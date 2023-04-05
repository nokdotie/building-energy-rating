package ie.deed.ber.api.model

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  Certificate as InternalCertificate
}
import ie.seai.ber.certificate.PdfCertificate

case class Certificate(
    number: Int,
    rating: String,
    ratingImageUrl: String,
    issuedOn: String,
    validUntil: String,
    address: String,
    energyRatingInKilowattHourPerSquareMetrePerYear: Float,
    carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear: Float
)

object Certificate {
  def fromInternal(internal: InternalCertificate): Option[Certificate] =
    internal.seaiIePdfCertificate
      .map { pdf =>
        Certificate(
          number = internal.number.value,
          rating = pdf.rating.toString,
          ratingImageUrl =
            s"https://ber.deed.ie/static/images/ber/${pdf.rating}.svg",
          issuedOn = pdf.issuedOn.toString,
          validUntil = pdf.validUntil.toString,
          address = pdf.propertyAddress.value,
          energyRatingInKilowattHourPerSquareMetrePerYear =
            pdf.energyRating.value,
          carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
            pdf.carbonDioxideEmissionsIndicator.value
        )
      }

  implicit val encoder: JsonEncoder[Certificate] =
    DeriveJsonEncoder.gen[Certificate]
}
