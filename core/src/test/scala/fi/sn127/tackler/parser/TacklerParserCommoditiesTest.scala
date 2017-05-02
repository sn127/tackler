package fi.sn127.tackler.parser

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

import fi.sn127.tackler.core.Settings

class TacklerParserCommoditiesTest extends FlatSpec {

  val tt = new TacklerTxns(new Settings(Paths.get(""), ConfigFactory.empty()))

  behavior of "Units and Commodities"

  /**
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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

  /**
   * test:uuid: todo
   */
  it should "uac opening with PnL" in {
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
   * test:uuid: todo
   */
  it should "uac opening with PnL ; comment" in {
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
   * test:uuid: todo
   */
  it should "uac closing position with PnL" in {
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
   * test:uuid: todo
   */
  it should "uac closing position with PnL ; comment" in {
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
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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
   * test:uuid: todo
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
