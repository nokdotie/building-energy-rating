package ie.nok.ber.auth.model

import java.time.Instant

case class UserRequest(email: String, timestamp: Instant, request: String)
