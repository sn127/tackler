/*
 * Copyright 2017 Jani Averbach
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

import org.scalatest.FlatSpec

import fi.sn127.tackler.core.Settings

class TacklerParserCommoditiesTest extends FlatSpec {

  val tt = new TacklerTxns(Settings())

  behavior of "Units and Commodities"

  /**
   * parse-only test
   */
  it should "uac" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * parse-only test
   */
  it should "uac ; comment" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD; comment
        | a
        |
        |2017-01-01
        | e   1 USD ; comment
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * parse-only test
   */
  it should "uac closing position" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD @ 1.20 EUR
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * parse-only test
   */
  it should "uac closing position ; comment" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD @ 1.20 EUR; comment
        | a
        |
        |2017-01-01
        | e   1 USD @ 1.20 EUR ; comment
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  behavior of "Profit and Loss parsing"

  /**
   * test:uuid: 9f711991-c9ae-4558-923c-95a69faff8bc
   */
  it should "opening with PnL" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR}
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * test:uuid: 92f75975-061b-4867-87f5-e25cf5b13d40
   */
  it should "opening with PnL ; comment" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR}; comment
        | a
        |
        |2017-01-01
        | e   1 USD {1.20 EUR} ; comment
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * test:uuid: 84d81380-8664-45d7-a9e1-523c38c7a963
   */
  it should "closing position with PnL" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR} @ 1.09 EUR
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  /**
   * test:uuid: c1fbac7b-e924-4eee-aed3-b11b51116f1a
   */
  it should "closing position with PnL ; comment" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.20 EUR} @ 1.09 EUR; comment
        | a
        |
        |2017-01-01
        | e   1 USD {1.20 EUR} @ 1.09 EUR ; comment
        | a
        |
        |""".stripMargin

    tt.string2Txns(txnStr)
  }

  behavior of "with invalid input"
  /**
   * test:uuid: 4babf379-9d88-49f3-8158-b9b7ff4e6eed
   */
  it should "perr: with commodity" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD EUR
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }

  /**
   * test:uuid: 0d1beaf2-c30c-4008-943f-46aaf44e4f76
   */
  it should "perr: with closing (comm)" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD @ 2
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }

  /**
   * test:uuid: 3152ec2f-4d5f-4a0a-b88c-68f17bccf7c6
   */
  it should "perr: with closing (value)" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD @ EUR
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }

  /**
   * test:uuid: bed02ea9-4191-4c98-b847-6b4e2a0fcb2d
   */
  it should "perr: with opening (comm)" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.00} @ 1.20 EUR
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }

  /**
   * test:uuid: ac4a6183-fb21-4847-8b3e-912f21fe5a6b
   */
  it should "perr: with opening (value)" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {EUR} @ 1.20 EUR
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }

  /**
   * test:uuid: 436d9ed5-b7a0-4e37-a7b4-86b00eb60e83
   */
  it should "perr: with missing @" in {
    val txnStr =
      """
        |2017-01-01
        | e   1 USD {1.00 EUR}  1.20 EUR
        | a
        |
        |""".stripMargin

    //val ex =
    intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    // assert(ex.getMessage === todo specific message
  }
}
