/*
 * Copyright 2016-2018 sn127.fi
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

import io.circe.Json
import io.circe.syntax._

import fi.sn127.tackler.api.{BalanceGroupReport, Metadata}
import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{Transaction, TxnData, TxnTS}


class BalanceGroupReporter(val mySettings: BalanceGroupSettings) extends BalanceReporterLike(mySettings) {

  override val name: String = mySettings.outputname

  protected def txtBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Seq[String] = {

    val header = List(
      metadata.fold(""){md => md.text()},
      mySettings.title,
      "-" * mySettings.title.length)

    val body = balGrps.par.flatMap(bal => txtBalanceGroup(bal))

    header ++ body
  }

  protected def txtBalanceGroup(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)
    val title = bal.title
    val  header = List(
      title,
      "-" * title.length)

    header ++ body ++ List("=" * footer.split("\n").head.length) ++ List(footer)
  }

  protected def getBalanceGroups(txnData: TxnData): Seq[Balance] = {

    val balanceFilter = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    val groupOp = mySettings.groupBy match {
      case GroupByYear() => { txn: Transaction =>
        TxnTS.isoYear(txn.header.timestamp)
      }
      case GroupByMonth() => { txn: Transaction =>
        TxnTS.isoMonth(txn.header.timestamp)
      }
      case GroupByDate() => { txn: Transaction =>
        TxnTS.isoDate(txn.header.timestamp)
      }
      case GroupByIsoWeek() => { txn: Transaction =>
        TxnTS.isoWeek(txn.header.timestamp)
      }
      case GroupByIsoWeekDate() => { txn: Transaction =>
        TxnTS.isoWeekDate(txn.header.timestamp)
      }
    }

    Accumulator.balanceGroups(txnData, groupOp, balanceFilter)
  }

  protected def jsonBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Json = {

    val bgs = balGrps.par.map(balanceToApi).seq

    BalanceGroupReport(metadata, mySettings.title, bgs).asJson
  }

  override
  def jsonReport(txnData: TxnData): Json = {
    val balGrps = getBalanceGroups(txnData)
    jsonBalanceGroupReport(txnData.metadata, balGrps)
  }

  override
  def writeReport(formats: Formats, txnData: TxnData): Unit = {

    val balGrps = getBalanceGroups(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          doRowOutputs(writers, txtBalanceGroupReport(txnData.metadata, balGrps))

        case JsonFormat() => {
          doRowOutputs(writers, Seq(jsonBalanceGroupReport(txnData.metadata, balGrps).pretty(printer)))
        }
      }
    })
  }
}
