package ie.deed.ber.common.certificate

import scala.util.{Try, Failure, Success}

enum Rating {
  case A1, A2, A3
  case B1, B2, B3
  case C1, C2, C3
  case D1, D2
  case E1, E2
  case F
  case G
}

object Rating {
  import Rating._

  def tryFromString(value: String): Try[Rating] =
    value match {
      case "A1"    => Success(A1)
      case "A2"    => Success(A2)
      case "A3"    => Success(A3)
      case "B1"    => Success(B1)
      case "B2"    => Success(B2)
      case "B3"    => Success(B3)
      case "C1"    => Success(C1)
      case "C2"    => Success(C2)
      case "C3"    => Success(C3)
      case "D1"    => Success(D1)
      case "D2"    => Success(D2)
      case "E1"    => Success(E1)
      case "E2"    => Success(E2)
      case "F"     => Success(F)
      case "G"     => Success(G)
      case unknown => Failure(Exception(s"Unknown rating: $unknown"))
    }
}
