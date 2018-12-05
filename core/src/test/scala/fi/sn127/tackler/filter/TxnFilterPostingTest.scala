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

import org.scalatest.FlatSpecLike

import fi.sn127.tackler.api._
import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.parser.TacklerTxns

class TxnFilterPostingTest extends TxnFilterSpec with FlatSpecLike {
  val tt = new TacklerTxns(Settings())

  val uuidTxn01 = "22e17bf5-3da5-404d-aaff-e3cc668191ee"
  val uuidTxn02 = "a88f4981-ebe7-4287-a59c-d444e3bd579a"
  val uuidTxn03 = "16cf7363-45d2-480c-ac49-c710f4ea5f0d"
  val uuidTxn04 = "205d4a48-471c-4015-856c-1c827f8befdd"
  val uuidTxn05 = "c6e6cb30-858b-4d9f-8b87-3fae2372136a"

  val txnStr =
    s"""2018-01-01 abc txn01
       | ;:uuid: ${uuidTxn01}
       | ; xyz
       | e:b  1.000000001
       | a
       |
       |2018-02-01 (abc) txn02
       | ;:uuid: ${uuidTxn02}
       | e:b  1.0000000011 ; abc hamburger
       | a
       |
       |2018-03-01 (xyz) txn03
       | ;:uuid: ${uuidTxn03}
       | ; xyz
       | ; abc
       | ; klm
       | e:b  2 EUR
       | a
       |
       |2018-04-01
       | ;:uuid: ${uuidTxn04}
       | e:b:abc  3 ; xyz
       | a
       |
       |2018-05-01
       | ;:uuid: ${uuidTxn05}
       | e:abc:foo  4 EUR
       | a
       |
       |""".stripMargin

  val txnsAll = tt.string2Txns(txnStr)

  behavior of "Transaction Posting filters"

  /**
   * test: 7784049f-ef3e-4185-8d33-f8c78478eef1
   */
  it must "filter by account name with wildcard at begin" in {
    val txnFilter = TxnFiltersAND(List(TxnFilterPostingAccount(".*:abc")))

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn04))
  }

  /**
   * test: 0a1e4848-cef0-46ec-9a50-cc209c45da63
   */
  it must "filter by account name with wildcard at end " in {
    val txnFilter = TxnFilterPostingAccount("e:abc.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn05))
  }

  /**
   * test: 0c1dcffe-152d-4959-89bb-2c48677ad171
   */
  it must "filter by posting comments" in {
    val txnFilter = TxnFilterPostingComment("abc.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn02))
  }

  /**
   * test: de72fb67-14a7-4032-b2c2-b1049ecd0c35
   */
  it must "filter by posting amount (exact)" in {
    val txnFilter = TxnFilterPostingAmountEqual("e:.*", BigDecimal(1.000000001))

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn01))
  }

  /**
   * test: 315d5ac3-28cf-417e-98bb-b738f209f5da
   */
  it must "filter by posting amount (less)" in {
    val txnFilter = TxnFilterPostingAmountLess("e:.*", BigDecimal(2))

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTxn01))
    assert(checkUUID(txnData, uuidTxn02))
  }

  /**
   * test: b94b99d7-acfa-4a4b-871f-c1b6282738ff
   */
  it must "filter by posting amount (greater)" in {
    val txnFilter = TxnFilterPostingAmountGreater("e:.*", BigDecimal(2))

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTxn04))
    assert(checkUUID(txnData, uuidTxn05))
  }

  /**
   * test: cfb795cd-d323-4181-a76a-1e5ce957add7
   */
  it must "filter by posting commodity" in {
    val txnFilter = TxnFilterPostingCommodity("EU.*")

    val txnData = txnsAll.filter(TxnFilterRoot(txnFilter))

    assert(txnData.txns.size === 2)
    assert(checkUUID(txnData, uuidTxn03))
    assert(checkUUID(txnData, uuidTxn05))
  }
}
