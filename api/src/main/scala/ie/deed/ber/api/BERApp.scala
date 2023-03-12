package ie.deed.ber.api

import ie.deed.ber.common.dao.BERRecordDao
import ie.deed.ber.common.model.*
import zio.http.*
import zio.http.model.*
import zio.json.*
import BERRecordCodecs.*

class BERApp(dao: BERRecordDao) {

  val http: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> _ / "ber" / int(certificateNumber) =>
      dao
        .getByBerNumber(certificateNumber)
        .map(berRecord => Response.json(berRecord.toJson))
        .getOrElse(Response(Status.NotFound))
    case Method.GET -> _ / "eircode" / eirCode / "ber" =>
      dao
        .getByEirCode(eirCode)
        .map(berRecord => Response.json(berRecord.toJson))
        .getOrElse(Response(Status.NotFound))
  }
}
