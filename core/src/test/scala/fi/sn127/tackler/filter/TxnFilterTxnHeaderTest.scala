/*
 * Copyright 2018 SN127.fi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fi.sn127.tackler.filter

import java.util.UUID

import org.scalatest.FlatSpecLike

import fi.sn127.tackler.api.{TxnFilterRoot, TxnFilterTxnCode, TxnFilterTxnComments, TxnFilterTxnDescription, TxnFilterTxnUUID}
import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.parser.TacklerTxns

class TxnFilterTxnHeaderTest extends TxnFilterSpec with FlatSpecLike {
  val tt = new TacklerTxns(Settings())

  val uuidTxn01 = "22e17bf5-3da5-404d-aaff-e3cc668191ee"
  val uuidTxn02 = "a88f4981-ebe7-4287-a59c-d444e3bd579a"
  val uuidTxn03 = "16cf7363-45d2-480c-ac49-c710f4ea5f0d"
  val uuidTxn04 = "205d4a48-471c-4015-856c-1c827f8befdd"

  val txnStr =
    s"""2018-01-01 abc txn01
       | ;:uuid: ${uuidTxn01}
       | ; xyz
       | e  1
       | a
       |
       |2018-02-01 (abc) txn02
       | ;:uuid: ${uuidTxn02}
       | e  1
       | a
       |
       |2018-03-01 (xyz) txn03
       | ;:uuid: ${uuidTxn03}
       | ; xyz
       | ; abc
       | ; klm
       | e  1
       | a
       |
       |2018-04-01
       | ;:uuid: ${uuidTxn04}
       | e:b:abc  1
       | a
       |
       |""".stripMargin

  val txnsAll = tt.string2Txns(txnStr)

  behavior of "Transaction filters"

  /**
   * test: 59157c61-0ced-4b3a-ab8d-ec5edf7aafb4
   */
  it must "filter by txn description" in {
    val txnFilter = TxnFilterTxnDescription("abc.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn01))
  }

  /**
   * test: 54c746cf-916f-4c24-8e53-d4306917a200
   */
  it must "filter by txn code" in {
    val txnFilter = TxnFilterTxnCode("ab.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn02))
  }

  /**
   * test: f6f2853b-fce4-4577-8fc3-3089e717de0b
   */
  it must "filter by txn UUID" in {
    val txnFilter = TxnFilterTxnUUID(UUID.fromString(uuidTxn02))

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn02))
  }

  /**
   * test: 6bf82dff-374a-4bf2-bdad-a882b59df932
   */
  it must "filter by txn UUID with no UUID" in {
    val txnNoUUIDStr =
      s"""2018-01-01 abc txn01
         | ; xyz
         | e  1
         | a
         |
         |2018-04-01
         | e:b:abc  1
         | a
         |
         |""".stripMargin

    val txnsNoUUIDAll = tt.string2Txns(txnNoUUIDStr)

    val txnFilter = TxnFilterTxnUUID(UUID.fromString(uuidTxn02))

    val txnData = txnsNoUUIDAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 0)
  }

  /**
   * test: 8bad2776-51fa-4766-839a-1bb99df44f5c
   */
  it must "filter by txn comments" in {
    val txnFilter = TxnFilterTxnComments("ab.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn03))
  }
}
