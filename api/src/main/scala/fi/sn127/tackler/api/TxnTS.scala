/*
 * Copyright 2016-2018 SN127.fi
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
package fi.sn127.tackler.api

import java.time.ZonedDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, SignStyle}
import java.time.temporal.ChronoField.DAY_OF_WEEK
import java.time.temporal.IsoFields

object TxnTS {

  private val frmtISOWeek = new DateTimeFormatterBuilder()
    .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral("-W")
    .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
    .appendOffset("+HH:MM", "Z")
    .toFormatter

  // no zoneId as with ISO_WEEK_DATE
  // no localized day number as with 'e' (e.g. en_US => sunday == 1)
  private val frmtISOWeekDate = new DateTimeFormatterBuilder()
    .appendValue(IsoFields.WEEK_BASED_YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral("-W")
    .appendValue(IsoFields.WEEK_OF_WEEK_BASED_YEAR, 2)
    .appendLiteral('-')
    .appendValue(DAY_OF_WEEK, 1)
    .appendOffset("+HH:MM", "Z")
    .toFormatter

  /**
   * ISO-8601 Timestamp with offset.
   *
   * @return ISO-8601 date-time: 2016-12-17T12:31:12+03:00
   */
  def isoZonedTS(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
  }

  /**
   * ISO-8601 date with offset.
   *
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016-12-17+03:00
   */
  def isoDate(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ISO_OFFSET_DATE)
  }

  /**
   * ISO-8601 year with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016+03:00
   */
  def isoYear(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyyXXX"))
  }

  /**
   * ISO-8601 mont with offset.
   *
   * @param ts timestamp
   * @return ISO-8601 date: 2016-12-17T12:31:12+03:00 => 2016-12+03:00
   */
  def isoMonth(ts: ZonedDateTime): String = {
    ts.format(DateTimeFormatter.ofPattern("yyyy'-'MMXXX"))
  }

  /**
   * ISO-8601 Week with offset.
   *
   * @return ISO-8601 week (without date): 2010-01-01 => 2009-W53+03:00
   */
  def isoWeek(ts: ZonedDateTime): String = {
    ts.format(frmtISOWeek)
  }

  /**
   * ISO-8601 Week date with offset.
   *
   * @return ISO-8601 week date: 2010-01-01 => 2009-W53-5+03:00
   */
  def isoWeekDate(ts: ZonedDateTime): String = {
    ts.format(frmtISOWeekDate)
  }
}
