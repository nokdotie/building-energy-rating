package ie.deed.ecad

final case class Latitude(value: BigDecimal) extends AnyVal
final case class Longitude(value: BigDecimal) extends AnyVal

case class GeographicCoordinate(
    latitude: Latitude,
    longitude: Longitude
)
