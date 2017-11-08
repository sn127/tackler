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

import io.circe._
import io.circe.syntax._

import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{BalanceTreeNode, Transaction, TxnData, TxnTS}

trait BalanceReportLike extends ReportLike {

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def txtBalanceBody(balance: Balance): (Seq[String], String) = {

    if (balance.isEmpty) {
      (Seq.empty[String], "")
    } else {
      val acclen = List(12,
        //todo: balance: delta handling
        ("%" + getScaleFormat(balance.deltas.head._2)).format(balance.deltas.head._2).length,
        balance.bal.map(b => ("%" + getScaleFormat(b.accountSum)).format(b.accountSum).length).max).max

      val subAcclen = balance.bal.map(b => ("%" + getScaleFormat(b.subAccTreeSum)).format(b.subAccTreeSum).length).max

      val body = balance.bal
        .map(b => {
          " " * 9 +
            fillFormat(acclen, b.accountSum) +
            " " * 3 +
            fillFormat(subAcclen, b.subAccTreeSum) +
            " " + b.acctn.commodity.map(c => c.name + " ").getOrElse("") + b.acctn.account
        })

      val footer = balance.deltas.toSeq.sortBy({case (cOpt, v) =>
        cOpt.map(c => c.name).getOrElse("")
      }).map({case (cOpt, v) =>
        " " * 9 + fillFormat(acclen, v) + cOpt.map(c => " " + c.name).getOrElse("")
      }).mkString("\n")

      (body, footer)
    }
  }

  implicit val encBalanceTreeNode: Encoder[BalanceTreeNode] = (as: BalanceTreeNode) => {
    as.acctn.commodity.fold(
      // no commodity
      Json.obj(
        ("accountSum", scaleFormat(as.accountSum).asJson),
        ("accountTreeSum", scaleFormat(as.subAccTreeSum).asJson),
        ("account", as.acctn.account.asJson))
    )(commodity =>
      Json.obj(
        ("accountSum", scaleFormat(as.accountSum).asJson),
        ("accountTreeSum", scaleFormat(as.subAccTreeSum).asJson),
        ("account", as.acctn.account.asJson),
        ("commodity", commodity.name.asJson))
    )
  }

  implicit val encBalance: Encoder[Balance] = (bal: Balance) => {
    val (body, deltas) = jsonBalanceBody(bal)

    Json.obj(
      ("title", bal.title.asJson),
      ("balanceRows", body.asJson),
      ("deltas", deltas.asJson)
    )
  }

  protected def jsonBalanceBody(balance: Balance): (Seq[Json], Seq[Json]) = {
    if (balance.isEmpty) {
      (Seq.empty[Json], Seq.empty[Json])
    } else {
      val body = balance.bal.map(_.asJson(encBalanceTreeNode))

      val deltas = balance.deltas.toSeq.sortBy({case (cOpt, v) =>
        cOpt.map(c => c.name).getOrElse("")
      }).map({case (cOpt, v) =>
        cOpt.fold(
          Json.obj(
            ("delta", scaleFormat(v).asJson))
        )(commodity =>
          Json.obj(
            ("delta", scaleFormat(v).asJson),
            ("commodity", cOpt.map(c => c.name.asJson).getOrElse(Json.Null))
        ))
      })

      (body, deltas)
    }
  }
}

class BalanceReport(val name: String, val settings: Settings) extends  BalanceReportLike {
  private val mySettings = settings.Reports.Balance

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def txtBalance(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)

    val  header = List(
      bal.metadata.fold(""){md => md.text()},
      bal.title,
      "-" * bal.title.length)

    if (body.isEmpty) {
      header
    } else {
      header ++ body ++ List("=" * footer.split("\n").head.length) ++ List(footer)
    }
  }

  protected def jsonBalanceReport(bal: Balance): Seq[String] = {
    Seq(bal.metadata.fold(
      Json.obj(("balance", bal.asJson(encBalance)))
    )({ md =>
      Json.obj(
        ("metadata", md.asJson()),
        ("balance", bal.asJson(encBalance))
      )
    }).spaces2)
  }

  protected def txtReporter(txns: TxnData)(reporter: (Balance) => Seq[String]): Seq[String] = {
    val bf = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }
    val bal = Balance(mySettings.title, txns, bf)
    reporter(bal)
  }

  def doReport(formats: Formats, txnData: TxnData): Unit = {

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() => {
          val txtBalanceReport = txtReporter(txnData)(txtBalance)
          doRowOutputs(writers, txtBalanceReport)
        }
        case JsonFormat() => {
          val balanceReport = txtReporter(txnData)(jsonBalanceReport)
          doRowOutputs(writers, balanceReport)
        }
      }
    })
  }
}


class BalanceGroupReport(val name: String, val settings: Settings) extends BalanceReportLike {
  private val mySettings = settings.Reports.BalanceGroup

  protected def txtBalanceGroups(txnData: TxnData): Seq[String] = {

    val balanceFilter = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    val header = List(
      txnData.metadata.fold(""){md => md.text()},
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

    val body = Accumulator.balanceGroups(txnData, groupOp, balanceFilter)
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
      // todo: refactor with balance
      header ++ body ++ List("=" * footer.split("\n").head.length) ++ List(footer)
    }
  }

  override
  def doReport(formats: Formats, txnData: TxnData): Unit = {

    val txtBalgrpReport = txtBalanceGroups(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          doRowOutputs(writers, txtBalgrpReport)

        case JsonFormat() => ???
      }
    })
  }
}
