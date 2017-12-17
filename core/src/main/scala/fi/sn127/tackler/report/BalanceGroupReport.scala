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

import io.circe.Json
import io.circe.syntax._

import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{Metadata, Transaction, TxnData, TxnTS}


class BalanceGroupReport(val mySettings: BalanceGroupSettings) extends BalanceReportLike(mySettings) {

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
    val title = bal.title.getOrElse("")
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
        TxnTS.isoYear(txn.date)
      }
      case GroupByMonth() => { txn: Transaction =>
        TxnTS.isoMonth(txn.date)
      }
      case GroupByDate() => { txn: Transaction =>
        TxnTS.isoDate(txn.date)
      }
      case GroupByIsoWeek() => { txn: Transaction =>
        TxnTS.isoWeek(txn.date)
      }
      case GroupByIsoWeekDate() => { txn: Transaction =>
        TxnTS.isoWeekDate(txn.date)
      }
    }

    Accumulator.balanceGroups(txnData, groupOp, balanceFilter)
  }

  protected def jsonBalanceGroups(balGrps: Seq[Balance]): (String, Json) = {
    ("balanceGroups", balGrps.par.map(bal => bal.asJson(encBalance)).seq.asJson)
  }

  protected def jsonBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Json = {
    metadata.fold(
      Json.obj(
        jsonTitle(mySettings.title),
        jsonBalanceGroups(balGrps))
    )({ md =>
      Json.obj(
        jsonTitle(mySettings.title),
        ("metadata", md.asJson()),
        jsonBalanceGroups(balGrps)
      )
    })
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

        case JsonFormat() =>
          doRowOutputs(writers, Seq(jsonBalanceGroupReport(txnData.metadata, balGrps).spaces2))
      }
    })
  }
}
