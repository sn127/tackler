/*
 * Copyright 2016-2017 Jani Averbach
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
package fi.sn127.tackler.parser

import java.nio.file.Paths
import java.time.ZoneId

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

import fi.sn127.tackler.core.{Settings, TxnException}

class TacklerTxnsTest extends FlatSpec {

  behavior of "TacklerTxns with String"

  /**
   * test: 52836ff9-94de-4575-bfae-6b5afa971351
   */
  it should "notice unbalanced transaction" in {
    val txnStr =
      """2017-01-01 str
        | e  1
        | a  1
        |""".stripMargin

    val ex = intercept[TxnException]{
      val tt = new TacklerTxns(new Settings(Paths.get(""), ConfigFactory.empty()))

      tt.input2Txns(txnStr)
    }
    assert(ex.getMessage === "TXN postings do not zero: 2")
  }

  /**
   * test: 200aad57-9275-4d16-bdad-2f1c484bcf17
   */
  it should "handle multiple txns" in {
    val txnStr =
      """2017-01-03 str3
        | e  1
        | a
        |
        |2017-01-01 str1
        | e  1
        | a
        |
        |2017-01-02 str2
        | e  1
        | a
        |
        |""".stripMargin

    val tt = new TacklerTxns(new Settings(Paths.get(""), ConfigFactory.empty()))

    val txns = tt.input2Txns(txnStr)
    assert(txns.length === 3)
    assert(txns.head.desc.getOrElse("") === "str1")
    assert(txns.last.desc.getOrElse("") === "str3")
  }
}
