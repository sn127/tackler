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

import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{Transaction, TxnTS, Txns}

trait BalanceReportLike extends ReportLike {
  protected def txtBalanceBody(balance: Balance): (Seq[String], String) = {

    if (balance.isEmpty) {
      (Seq.empty[String], "")
    } else {
      val acclen = List(12,
        ("%" + getScaleFormat(balance.delta)).format(balance.delta).length,
        balance.bal.map(b => ("%" + getScaleFormat(b.accountSum)).format(b.accountSum).length).max).max

      val subAcclen = balance.bal.map(b => ("%" + getScaleFormat(b.subAccTreeSum)).format(b.subAccTreeSum).length).max

      val body = balance.bal
        .map(b => {
          " " * 9 +
            fillFormat(acclen, b.accountSum) +
            " " * 3 +
            fillFormat(subAcclen, b.subAccTreeSum) +
            " " + b.acctn.account
        })

      val footer = " " * 9 + fillFormat(acclen, balance.delta)
      (body, footer)
    }
  }
}

class BalanceReport(val name: String, val settings: Settings) extends  BalanceReportLike {
  private val mySettings = settings.Reports.Balance

  protected def txtBalance(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)

    val  header = List(
      bal.title,
      "-" * bal.title.length)

    if (body.isEmpty) {
      header
    } else {
      header ++ body ++ List("=" * footer.length) ++ List(footer)
    }
  }


  protected def txtReporter(txns: Txns)(reporter: (Balance) => Seq[String]): Seq[String] = {
    val bf = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }
    val bal = Balance(mySettings.title, txns, bf)
    reporter(bal)
  }

  def doReport(formats: Formats, txns: Txns): Unit = {
    val txtBalanceReport = txtReporter(txns)(txtBalance)

    formats.foreach({case (format, w ) =>
      format match {
        case TextFormat() =>
          textWriter(w, txtBalanceReport)

        //case JsonFormat() => ???
      }
    })
  }
}


class BalanceGroupReport(val name: String, val settings: Settings) extends BalanceReportLike {
  private val mySettings = settings.Reports.BalanceGroup

  protected def txtBalanceGroups(txns: Txns): Seq[String] = {

    val balanceFilter = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    val header = List(
      mySettings.title,
      "-" * mySettings.title.length)

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

    val body = Accumulator.balanceGroups(txns, groupOp, balanceFilter)
      .par.flatMap(bal => txtBalanceGroup(bal))

    header ++ body
  }

  protected def txtBalanceGroup(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)

    val  header = List(
      bal.title,
      "-" * bal.title.length)

    if (body.isEmpty) {
      Nil
    } else {
      header ++ body ++ List("=" * footer.length) ++ List(footer)
    }
  }

  override
  def doReport(formats: Formats, txns: Txns): Unit = {

    val txtBalgrpReport = txtBalanceGroups(txns)

    formats.foreach({case (format, w ) =>
      format match {
        case TextFormat() =>
          textWriter(w, txtBalgrpReport)

        //case JsonFormat() => ???
      }
    })
  }
}
