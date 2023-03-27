package ie.deed.ber.api.model

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import ie.deed.ber.common.certificate.{
  CertificateNumber,
  Certificate as InternalCertificate
}
import ie.seai.ber.certificate.{HtmlCertificate, PdfCertificate}

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
    internal.seaiIeHtmlCertificate
      .orElse(internal.seaiIePdfCertificate)
      .collect {
        case certificate: HtmlCertificate =>
          (
            certificate.rating,
            certificate.issuedOn,
            certificate.validUntil,
            certificate.propertyAddress,
            certificate.energyRating,
            certificate.carbonDioxideEmissionsIndicator
          )
        case certificate: PdfCertificate =>
          (
            certificate.rating,
            certificate.issuedOn,
            certificate.validUntil,
            certificate.propertyAddress,
            certificate.energyRating,
            certificate.carbonDioxideEmissionsIndicator
          )
      }
      .map {
        (
            rating,
            issuedOn,
            validUntil,
            propertyAddress,
            energyRating,
            carbonDioxideEmissionsIndicator
        ) =>
          Certificate(
            number = internal.number.value,
            rating = rating.toString,
            ratingImageUrl =
              s"https://ber.deed.ie/static/images/ber/$rating.svg",
            issuedOn = issuedOn.toString,
            validUntil = validUntil.toString,
            address = propertyAddress.value,
            energyRatingInKilowattHourPerSquareMetrePerYear =
              energyRating.value,
            carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
              carbonDioxideEmissionsIndicator.value
          )
      }

  implicit val encoder: JsonEncoder[Certificate] =
    DeriveJsonEncoder.gen[Certificate]
}
