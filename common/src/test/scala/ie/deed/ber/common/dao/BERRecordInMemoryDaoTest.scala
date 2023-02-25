package ie.deed.ber.common.dao

import ie.deed.ber.common.model.BERRecord
import munit.FunSuite

class BERRecordInMemoryDaoTest extends FunSuite {

  test("getByBerNumber") {
    val actual = BERRecordInMemoryDao.getByBerNumber(100469758)
    assertNotEquals(actual, Option.empty[BERRecord])
    assertEquals(actual.map(_.number), Some(100469758))
  }
}
