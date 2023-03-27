package ie.deed.ber.api.model

import zio.json.{DeriveJsonEncoder, JsonEncoder}

import ie.deed.ber.common.certificate.{
  CertificateNumber,
  CertificateStore,
  Certificate as InternalCertificate
}
case class Certificate(
    number: Int,
    rating: String,
    issuedOn: String,
    validUntil: String,
    address: String,
    energyRatingInKilowattHourPerSquareMetrePerYear: Float,
    carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear: Float
)

object Certificate {
  def fromInternal(internal: InternalCertificate): Certificate = {
    val html = internal.seaiIeHtmlCertificate
    val pdf = internal.seaiIePdfCertificate

    Certificate(
      number = internal.number.value,
      rating = html
        .map(_.rating)
        .orElse(pdf.map(_.rating))
        .fold("") {
          _.toString
        },
      issuedOn = html
        .map(_.issuedOn)
        .orElse(pdf.map(_.issuedOn))
        .fold("") {
          _.toString
        },
      validUntil = html
        .map(_.validUntil)
        .orElse(pdf.map(_.validUntil))
        .fold("") {
          _.toString
        },
      address = html
        .map(_.propertyAddress)
        .orElse(pdf.map(_.propertyAddress))
        .fold("") {
          _.value
        },
      energyRatingInKilowattHourPerSquareMetrePerYear = html
        .map(_.energyRating)
        .orElse(pdf.map(_.energyRating))
        .fold(0f)(_.value),
      carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
        html
          .map(_.carbonDioxideEmissionsIndicator)
          .orElse(pdf.map(_.carbonDioxideEmissionsIndicator))
          .fold(0f)(_.value),
    )
  }

  implicit val encoder: JsonEncoder[Certificate] =
    DeriveJsonEncoder.gen[Certificate]
}
