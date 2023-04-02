package ie.deed.ber.auth.model

import java.time.Instant
import java.util.UUID

enum TokenType:
  case Admin, Dev, User

/**
token is DocumentId
 */
case class UserToken(email: String, token: String, tokenType: TokenType, createdAt: Instant)

/**
 email is DocumentId
 */
case class User(companyRef: String, email: String, name: Option[String], createdAt: Instant)

/**
id is DocumentId
 */
case class Credit(id: String, companyRef: Int, number: Int)

/**
id is DocumentId
 */
case class Company(id: Int, name: String, email: String, createdAt: Instant, users: List[User], credits: List[Credit])
