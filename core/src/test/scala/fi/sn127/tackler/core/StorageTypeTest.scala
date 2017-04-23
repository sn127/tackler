package fi.sn127.tackler.core

import org.scalatest.FlatSpec

class StorageTypeTest extends FlatSpec {

  behavior of "StorageType"

  it should "apply txn" in {
    val t = StorageType("txn")
    assert(t.isInstanceOf[FilesystemStorageType])
  }

  it should "apply git" in {
    val t = StorageType("git")
    assert(t.isInstanceOf[GitStorageType])
  }

  /**
   * test:uuid: 195971d7-f16f-4c1c-a761-6764b28fd4db
   */
  it should "handle unknown type" in {
    assertThrows[TacklerException]{
      StorageType("no-such-type")
    }
  }
}
