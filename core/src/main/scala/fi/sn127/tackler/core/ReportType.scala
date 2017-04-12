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
package fi.sn127.tackler.core

import fi.sn127.tackler.report.Writers

sealed trait ReportFormat {
  val ext: String

  /**
   * Add console output, if it is supported by this format
   * @param writers
   * @param consoles
   * @return
   */
  def consoleOutput(writers: Writers, consoles: Writers): Writers = {
    writers ++ consoles
  }
}

sealed case class TextFormat() extends ReportFormat {
  val ext: String = "txt"
}

/*
sealed case class JsonFormat() extends ReportFormat {
  val ext: String = "json"
}
*/
object ReportFormat {
  def apply(format: String): ReportFormat = {
    format match {
      case "txt" => TextFormat()
      //case "json" => JsonFormat()

      /* Error*/
      case frmt => throw new ReportException(
        "Unknown report format [" + frmt + "]. Valid formats are: txt, json")
    }
  }
}


sealed trait ReportType
sealed case class BalanceReportType() extends ReportType
sealed case class EquityReportType() extends ReportType
sealed case class BalanceGroupReportType() extends ReportType
sealed case class RegisterReportType() extends ReportType
sealed case class IdentityReportType() extends ReportType

object ReportType {
  def apply(groupBy: String): ReportType = {
    groupBy match {
      case "balance" => BalanceReportType()
      case "equity" => EquityReportType()
      case "balance-group" => BalanceGroupReportType()
      case "register" => RegisterReportType()
      case "identity" => IdentityReportType()
      /* Error*/
      case rpt => throw new ReportException(
        "Unknown report type [" + rpt + "]. Valid types are: balance, balance-group, register, identity")
    }
  }
}
