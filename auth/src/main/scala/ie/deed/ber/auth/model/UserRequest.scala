package ie.deed.ber.auth.model

import java.time.Instant

case class UserRequest (email: String, timestamp: Instant, request: String)
