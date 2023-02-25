package ie.deed.ber.common.dao

import ie.deed.ber.common.model.BERRecord

trait BERRecordDao {

  def getByBerNumber(number: Int): Option[BERRecord]

  def getByEirCode(eirCode: String): Option[BERRecord]
}


