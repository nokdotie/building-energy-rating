package ie.deed.ber.auth.model

import java.time.Instant

case class Credit(email: String, timestamp: Instant, number: Long)
