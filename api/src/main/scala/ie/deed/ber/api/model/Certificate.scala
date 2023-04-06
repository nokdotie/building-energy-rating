package ie.deed.ber.api.model

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import ie.deed.ber.common.certificate.{Certificate as InternalCertificate}

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
  def fromInternal(internal: InternalCertificate): Certificate =
    Certificate(
      number = internal.number.value,
      rating = internal.rating.toString,
      ratingImageUrl =
        s"https://ber.deed.ie/static/images/ber/${internal.rating}.svg",
      issuedOn = internal.issuedOn.toString,
      validUntil = internal.validUntil.toString,
      address = internal.propertyAddress.value,
      energyRatingInKilowattHourPerSquareMetrePerYear =
        internal.energyRating.value,
      carbonDioxideEmissionsIndicatorInKilogramOfCarbonDioxidePerSquareMetrePerYear =
        internal.carbonDioxideEmissionsIndicator.value
    )

  given JsonEncoder[Certificate] = DeriveJsonEncoder.gen[Certificate]
}
