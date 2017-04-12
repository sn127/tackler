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
package fi.sn127.tackler.model

import java.time.ZonedDateTime

import org.scalatest.FlatSpec

class TxnTSTest extends FlatSpec {

  def txt2ts(txtTS: String): ZonedDateTime = {
    ZonedDateTime.parse(txtTS,
      java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  behavior of "TxnTS"

  it should "isoDate" in {
    assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00Z")) ===  "2010-01-02Z")
    assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00+02:00")) ===  "2010-01-02+02:00")
    assert(TxnTS.isoDate(txt2ts("2010-01-02T00:00:00-02:00")) ===  "2010-01-02-02:00")
  }

  it should "isoMonth" in {
    assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00Z")) ===  "2010-01Z")
    assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00+02:00")) ===  "2010-01+02:00")
    assert(TxnTS.isoMonth(txt2ts("2010-01-02T00:00:00-02:00")) ===  "2010-01-02:00")
  }

  it should "isoYear" in {
    assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00Z")) ===  "2010Z")
    assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00+02:00")) ===  "2010+02:00")
    assert(TxnTS.isoYear(txt2ts("2010-01-02T00:00:00-02:00")) ===  "2010-02:00")
  }

  it should "isoZonedTS" in {
    assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T00:00:00+16:00")) ===  "2010-01-01T00:00:00+16:00")
    assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.456+16:00")) ===  "2010-01-01T01:02:03.456+16:00")
    assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.456789+16:00")) ===  "2010-01-01T01:02:03.456789+16:00")
    assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T01:02:03.700-16:00")) ===  "2010-01-01T01:02:03.7-16:00")

    assert(TxnTS.isoZonedTS(txt2ts("2010-01-01T00:00:00+00:00")) ===  "2010-01-01T00:00:00Z")
  }

  it should "isoWeek" in {
    assert(TxnTS.isoWeek(txt2ts("2010-01-03T00:00:00+00:00")) ===  "2009-W53Z")
    assert(TxnTS.isoWeek(txt2ts("2010-01-04T00:00:00+00:00")) ===  "2010-W01Z")
    assert(TxnTS.isoWeek(txt2ts("2017-01-01T00:00:00+00:00")) ===  "2016-W52Z")
    assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00+00:00")) ===  "2017-W01Z")

    assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00+02:00")) ===  "2017-W01+02:00")
    assert(TxnTS.isoWeek(txt2ts("2017-01-02T00:00:00-02:00")) ===  "2017-W01-02:00")
  }

  it should "isoWeekDate" in {
    assert(TxnTS.isoWeekDate(txt2ts("2010-01-03T00:00:00+00:00")) ===  "2009-W53-7Z")
    assert(TxnTS.isoWeekDate(txt2ts("2010-01-04T00:00:00+00:00")) ===  "2010-W01-1Z")
    assert(TxnTS.isoWeekDate(txt2ts("2017-01-01T00:00:00+00:00")) ===  "2016-W52-7Z")
    assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00+00:00")) ===  "2017-W01-1Z")
    assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00Z")) ===  "2017-W01-1Z")

    assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00+02:00")) ===  "2017-W01-1+02:00")
    assert(TxnTS.isoWeekDate(txt2ts("2017-01-02T00:00:00-02:00")) ===  "2017-W01-1-02:00")
  }
}
