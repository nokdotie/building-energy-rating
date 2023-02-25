package ie.deed.ber.api

import ie.deed.ber.common.dao.BERRecordDao
import ie.deed.ber.common.model.*
import zhttp.http.*
import zio.json.*
import BERRecordCodecs.*

class BERApp(dao: BERRecordDao) {

  val http: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> _ / "ber" / "by-number" / int(number) =>
      dao
        .getByBerNumber(number)
        .map(berRecord => Response.json(berRecord.toJsonPretty))
        .getOrElse(Response(Status.NotFound))
    case Method.GET -> _ / "ber" / "by-eir-code" / eirCode =>
      dao
        .getByEirCode(eirCode)
        .map(berRecord => Response.json(berRecord.toJsonPretty))
        .getOrElse(Response(Status.NotFound))
  }

}
