package ie.eircode.ecad

// This is essentially the Postal Address but the Post Town element is removed if it is only required for Postal purposes and the County is always the geographic County of the address.
final case class GeographicAddress(value: String) extends AnyVal
