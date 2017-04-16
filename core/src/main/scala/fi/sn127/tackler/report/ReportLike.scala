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

import fi.sn127.tackler.model.Txns

trait ReportLike extends OutputLike {
  /**
   * Report name part of output filename.
   */
  val name: String

  /**
   * Get decimal part of format string based on scale settings
   * and actual scale of value ("how many decimals").
   *
   * If scale is less than minScale, then minScale is used
   * If Scale is more than maxScale, then maxScale is used
   * if minScale < scale <= maxScale, then actual scale of value is used
   *
   * @param v value to be formatted
   * @return decimal part of format string (e.g. ".2f")
   */
  def getScaleFormat(v: BigDecimal): String = {
    ".%df".format(
      if (v.scale <= settings.minScale) {
        settings.minScale
      } else if (settings.minScale < v.scale && v.scale <= settings.maxScale) {
        v.scale
      } else {
        settings.maxScale
      }
    )
  }

  /**
   * get format string for value with automatic scale.
   * see [[getScaleFormat]]
   *
   * @param width of field
   * @param v value
   * @return format string (e.g. "%12.2f")
   */
  def getFillFormat(width: Int, v: BigDecimal): String = {
    "%" + "%d".format(width) + getScaleFormat(v)
  }

  /**
   * Format value with automatic scale
   * see [[getFillFormat]] and [[getScaleFormat]]
   *
   * @param width field width
   * @param v value to be formatted
   * @return value formatted as string
   */
  def fillFormat(width: Int, v: BigDecimal): String = {
    getFillFormat(width, v).format(v)
  }

  /**
   * Output multiple rows to multiple outputs
   *
   * @param writers sequence of outputs
   * @param rows to be output
   */
  protected def doRowOutputs(writers: Writers, rows: Seq[String]): Unit = {
    writers.foreach(w => {
      doRowOutput(w, rows)
    })
  }

  def doReport(formats: Formats, txns: Txns): Unit
}
