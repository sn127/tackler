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
package fi.sn127.tackler.report

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import io.circe.Json
import org.scalatest.FlatSpec

import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.model.TxnData

class ReportLikeTest extends FlatSpec {

  class Frmt(val name: String, val settings: Settings) extends ReportLike(new ReportSettings(settings)) {
    override def writeReport(formats: Formats, txns: TxnData): Unit = ???
    override def jsonReport(txnData: TxnData): Json = ???
  }

  val settings = new Settings(Paths.get(""), ConfigFactory.empty())

  val frmt = new Frmt("", settings)

  val sc0: scala.math.BigDecimal = 1
  val sc1: scala.math.BigDecimal = 1.1
  val sc2: scala.math.BigDecimal = 1.12
  val sc3: scala.math.BigDecimal = 1.123
  val sc6: scala.math.BigDecimal = 1.123456
  val sc8_tr7: scala.math.BigDecimal = 1.12345677
  val sc8_tr5: scala.math.BigDecimal = 1.12345675
  val sc8_tr4: scala.math.BigDecimal = 1.12345674

  val sc10_2: scala.math.BigDecimal = 1234567890.12
  val sc14_4: scala.math.BigDecimal = 12345678901234.1234

  val sc18_2 = BigDecimal(123456789123456789l) + BigDecimal(.12)
  val sc18_9 = BigDecimal("123456789123456789.123456789")

  behavior of "scaleFrmtStr"

  it should "DIR with minScale" in {
    assert(frmt.getScaleFormat(sc0) === ".2f")
    assert(frmt.getScaleFormat(sc1) === ".2f")
  }

  it should "DIR between min and max" in {
    assert(frmt.getScaleFormat(sc2) === ".2f")
    assert(frmt.getScaleFormat(sc3) === ".3f")
    assert(frmt.getScaleFormat(sc6) === ".6f")
  }

  it should "DIR with maxScale" in {
    assert(frmt.getScaleFormat(sc8_tr7) === ".7f")
  }

  behavior of "valueFillFrmtStr"

  it should "DIR with small and big values" in {
    assert(frmt.getFillFormat(5, sc1) === "%5.2f")
    assert(frmt.getFillFormat(5, sc3) === "%5.3f")
    assert(frmt.getFillFormat(18, sc8_tr7) === "%18.7f")
    assert(frmt.getFillFormat(26, sc18_9) === "%26.7f")
  }

  behavior of "fillFormat"

  it should "format normal values" in {
    assert(frmt.scaleFormat(sc0) === "1.00")
    assert(frmt.scaleFormat(sc6) === "1.123456")
    assert(frmt.scaleFormat(sc10_2) === "1234567890.12")

    assert(frmt.fillFormat(1, sc0) === "1.00")
    assert(frmt.fillFormat(4, sc0) === "1.00")
    assert(frmt.fillFormat(5, sc0) === " 1.00")

    assert(frmt.fillFormat(9, sc6) === " 1.123456")
    assert(frmt.fillFormat(14, sc10_2) === " 1234567890.12")

  }

  it should "truncate values correctly" in {
    assert(frmt.scaleFormat(sc8_tr7) === "1.1234568")
    assert(frmt.scaleFormat(sc8_tr5) === "1.1234568")
    assert(frmt.scaleFormat(sc8_tr4) === "1.1234567")

    assert(frmt.fillFormat(10, sc8_tr7) === " 1.1234568")
    assert(frmt.fillFormat(10, sc8_tr5) === " 1.1234568")
    assert(frmt.fillFormat(10, sc8_tr4) === " 1.1234567")
  }


  it should "format large value" in {
    assert(frmt.scaleFormat(sc18_2) === "123456789123456789.12")
    assert(frmt.scaleFormat(sc18_9) === "123456789123456789.1234568")

    assert(frmt.fillFormat(22, sc18_2) === " 123456789123456789.12")
    assert(frmt.fillFormat(27, sc18_9) === " 123456789123456789.1234568")
  }
}
