package ie.deed.ber.api

import ie.deed.ber.common.dao.BERRecordDao
import ie.deed.ber.common.model.*
import zhttp.http.*
import zio.json.*
import BERRecordCodecs.*
import pdi.jwt.JwtClaim

import scala.annotation.unused

class BERAuthApp(dao: BERRecordDao) {

  def http(jwtClaim: JwtClaim): Http[Any, Nothing, Request, Response] =
    Http.collect[Request] {
      case Method.GET -> _ / "auth" / "ber" / int(certificateNumber) =>
        dao
          .getByBerNumber(certificateNumber)
          .map(berRecord => Response.json(berRecord.toJson))
          .getOrElse(Response(Status.NotFound))
      case Method.GET -> _ / "auth" / "eircode" / eirCode / "ber" =>
        dao
          .getByEirCode(eirCode)
          .map(berRecord => Response.json(berRecord.toJson))
          .getOrElse(Response(Status.NotFound))
    }
}
