/*
 * Copyright 2018 sn127.fi
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

import io.circe.parser.decode

import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.parser.TacklerTxns

class TxnFilterTreeJsonTest extends TxnFilterTest  {
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

  behavior of "Transaction filters"

  it must "json test" in {
    val filterJsonStr =
      """
        |{
        |  "txnFilter" : {
        |    "TxnFilterTreeAND" : {
        |      "txnFilters" : [
        |        {
        |          "TxnFilterPostingAccount" : {
        |            "regex" : ".*:abc"
        |          }
        |        }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin
    val txnFilterRoot = decode[TxnFilterRoot](filterJsonStr)

    val txnData = txnsAll.filter(txnFilterRoot.right.get)

    assert(txnData.txns.size === 1)
    assert(checkUUID(txnData, uuidTxn04))
  }
}
