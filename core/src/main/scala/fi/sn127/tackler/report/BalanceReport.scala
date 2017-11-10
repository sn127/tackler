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
import fi.sn127.tackler.model.{BalanceTreeNode, Metadata, Transaction, TxnData, TxnTS}

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

  implicit val encBalanceTreeNode: Encoder[BalanceTreeNode] = (btn: BalanceTreeNode) => {

    def jsonAccountSum = ("accountSum", scaleFormat(btn.accountSum).asJson)
    def jsonAccountTreeSum = ("accountTreeSum", scaleFormat(btn.subAccTreeSum).asJson)
    def jsonAccount = ("account", btn.acctn.account.asJson)

    btn.acctn.commodity.fold(
      Json.obj(
        jsonAccountSum,
        jsonAccountTreeSum,
        jsonAccount)
    )(commodity =>
      Json.obj(
        jsonAccountSum,
        jsonAccountTreeSum,
        jsonAccount,
        ("commodity", commodity.name.asJson))
    )
  }

  implicit val encBalance: Encoder[Balance] = (bal: Balance) => {

    val (body, deltas) = jsonBalanceBody(bal)

    def jsonBalanceRows = ("balanceRows", body.asJson)
    def jsonDeltas = ("deltas", deltas.asJson)

    bal.title.fold(
      Json.obj(
        jsonBalanceRows,
        jsonDeltas)
    )(title =>
      Json.obj(
        jsonTitle(title),
        jsonBalanceRows,
        jsonDeltas)
    )
  }

  protected def jsonBalanceBody(balance: Balance): (Seq[Json], Seq[Json]) = {
    val body = balance.bal.map(_.asJson(encBalanceTreeNode))

    val deltas = balance.deltas.toSeq
      .sortBy({ case (cOpt, v) =>
        cOpt.map(c => c.name).getOrElse("")
      })
      .map({ case (commodityOpt, v) => {
        def jsonDelta = ("delta", scaleFormat(v).asJson)

        commodityOpt.fold(
          Json.obj(jsonDelta)
        )(commodity =>
          Json.obj(
            jsonDelta,
            ("commodity", commodity.name.asJson))
        )
      }
      })

    (body, deltas)
  }
}

class BalanceReport(val name: String, val settings: Settings) extends  BalanceReportLike {
  private val mySettings = settings.Reports.Balance

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def txtBalanceReport(bal: Balance): Seq[String] = {

    val (body, footer) = txtBalanceBody(bal)

    val  header = List(
      bal.metadata.fold(""){md => md.text()},
      mySettings.title,
      "-" * mySettings.title.length)

    if (body.isEmpty) {
      header
    } else {
      header ++ body ++ List("=" * footer.split("\n").head.length) ++ List(footer)
    }
  }

  protected def jsonBalance(bal: Balance): (String, Json) = {
    ("balance", bal.asJson(encBalance))
  }

  protected def jsonBalanceReport(bal: Balance): Seq[String] = {
    Seq(bal.metadata.fold(
      Json.obj(
        jsonTitle(mySettings.title),
        jsonBalance(bal))
    )({ md =>
      Json.obj(
        jsonTitle(mySettings.title),
        ("metadata", md.asJson()),
        jsonBalance(bal)
      )
    }).spaces2)
  }

  protected def getBalance(txns: TxnData): Balance = {
    val bf = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    Balance(None, txns, bf)
  }

  def doReport(formats: Formats, txnData: TxnData): Unit = {

    val bal = getBalance(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() => {
          doRowOutputs(writers, txtBalanceReport(bal))
        }
        case JsonFormat() => {
          doRowOutputs(writers, jsonBalanceReport(bal))
        }
      }
    })
  }
}


class BalanceGroupReport(val name: String, val settings: Settings) extends BalanceReportLike {
  private val mySettings = settings.Reports.BalanceGroup

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
    ("balanceGroups", balGrps.par.map(bal => bal.asJson(encBalance)).to[Seq].asJson)
  }

  protected def jsonBalanceGroupReport(metadata: Option[Metadata], balGrps: Seq[Balance]): Seq[String] = {
    Seq(metadata.fold(
      Json.obj(
        jsonTitle(mySettings.title),
        jsonBalanceGroups(balGrps))
    )({ md =>
      Json.obj(
        jsonTitle(mySettings.title),
        ("metadata", md.asJson()),
        jsonBalanceGroups(balGrps)
      )
    }).spaces2)
  }

  override
  def doReport(formats: Formats, txnData: TxnData): Unit = {

    val balGrps = getBalanceGroups(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          doRowOutputs(writers, txtBalanceGroupReport(txnData.metadata, balGrps))

        case JsonFormat() =>
          doRowOutputs(writers, jsonBalanceGroupReport(txnData.metadata, balGrps))
      }
    })
  }
}
