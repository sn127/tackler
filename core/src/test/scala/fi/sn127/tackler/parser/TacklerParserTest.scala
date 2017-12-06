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

import org.scalatest.FlatSpec

import fi.sn127.tackler.core.Settings

class TacklerParserTest extends FlatSpec {

  val tt = new TacklerTxns(Settings())

  behavior of "String Input"

  /**
   * test: b591d5d3-0be8-4264-8ec0-75b464ff86dc
   */
  it should "accept valid input string" in {
    val txnStr =
      """
        |2017-01-01 str
        | e   1
        | a  -1
        |
        |""".stripMargin

    val txns = TacklerParser.txnsText(txnStr)

    assert(txns.txn(0).description().text().getText === "str")
  }

  /**
   * test: 641c44ab-0ac3-4247-b16e-a4acea5a78ec
   */
  it should "accept UFT-8 via string interface" in {
    val txnStr =
      """
        |2017-01-01 äöåÄÖÅéèÿ風空
        | e   1
        | a  -1
        |
        |""".stripMargin

    val txns = tt.string2Txns(txnStr)
    assert(txns.head.desc.getOrElse("") === "äöåÄÖÅéèÿ風空")
  }

  /**
   * test: b0d7d5b1-8927-43c4-80c1-bfda9d0b149f
   */
  it should "handle long String input in case of error" in {
    val txnStr = """2017-01-01 ()) str""" + " " * 1025

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage.length === 1097)
    assert(ex.getMessage.startsWith("Txn Parse Error: Invalid input: truncated inputStr(0, 1024)=[2017-01-01 ()) str  "), ex.getMessage)
  }

  behavior of "Invalid (code)"

  /**
   * test: 6880deae-8cbc-4b36-b148-feb3d4e71137
   */
  it should "error with TacklerParseError" in {
    val txnStr = """2017-01-01 ()) str"""

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage === "Txn Parse Error: Invalid input: [2017-01-01 ()) str], msg: null")
  }

  behavior of "Invalid UUID"

  /**
   * test: 4391990c-83f4-4ea2-8c25-78a87beae219
   */
  it should "detect missing uuid" in {
    val txnStr =
      """
        |2017-01-01 str
        | ;:uuid:
        | e   1
        | a  -1
        |
        |""".stripMargin

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage === """Txn Parse Error: Invalid input: [
      |2017-01-01 str
      | ;:uuid:
      | e   1
      | a  -1
      |
      |], msg: null""".stripMargin)
  }

  /**
   * test: 56042ba1-89ca-48da-a55a-d6fea2946c59
   */
  it should "notice invalid uuid" in {

    val txnStr =
      """
        |2017-01-01 str
        | ;:uuid: 77356f17-98c9-43c6b9a7-bfc7436b77c8
        | e   1
        | a  -1
        |
        |""".stripMargin
    val ex = intercept[RuntimeException]{
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage.startsWith("Invalid UUID string: "), ex.getMessage)
  }

  behavior of "Account names"

  /**
   * test: 9c836932-718c-491d-8cf0-30e35a0d1533
   */
  it should "error with missing part" in {
    val txnStr =
    """
      |2017-01-01 desc
      | a::b  1
      | e
      |
      |""".stripMargin

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage === """Txn Parse Error: Invalid input: [
      |2017-01-01 desc
      | a::b  1
      | e
      |
      |], msg: null""".stripMargin)
  }

  /**
   * test: 4a8f98d6-fb07-46ea-89ea-5ef4e7f4a3f8
   */
  it should "error with ':a'" in {
    val txnStr =
      """
        |2017-01-01 desc
        | :a  1
        | e
        |
        |""".stripMargin

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage === """Txn Parse Error: Invalid input: [
                               |2017-01-01 desc
                               | :a  1
                               | e
                               |
                               |], msg: line 3:1 no viable alternative at input ' :'""".stripMargin)

  }

  /**
   * test: d8e4e643-1b1b-46c7-b84c-e6cf04952971
   */
  it should "error with 'a:'" in {
    val txnStr =
      """
        |2017-01-01 desc
        | a:  1
        | e
        |
        |""".stripMargin

    val ex = intercept[TacklerParseException] {
      tt.string2Txns(txnStr)
    }
    assert(ex.getMessage === """Txn Parse Error: Invalid input: [
                               |2017-01-01 desc
                               | a:  1
                               | e
                               |
                               |], msg: null""".stripMargin)
  }

}
