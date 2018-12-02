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
package fi.sn127.tackler.report

import io.circe._
import io.circe.syntax._

import fi.sn127.tackler.api.{BalanceItem, BalanceReport, Delta, OrderByDelta}
import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{BalanceTreeNode, TxnData}

abstract class BalanceReporterLike(cfg: ReportSettings) extends ReportLike(cfg) {

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

      val footer = balance.deltas.toSeq.sortBy({case (cOpt, _) =>
        cOpt.map(c => c.name).getOrElse("")
      }).map({case (cOpt, v) =>
        " " * 9 + fillFormat(acclen, v) + cOpt.map(c => " " + c.name).getOrElse("")
      }).mkString("\n")

      (body, footer)
    }
  }

  protected def btnToApi(btn: BalanceTreeNode): BalanceItem = {
    BalanceItem(
      accountSum = scaleFormat(btn.accountSum),
      accountTreeSum = scaleFormat(btn.subAccTreeSum),
      account = btn.acctn.account,
      commodity = btn.acctn.commodity.map(_.name)
    )
  }

  protected def balanceToApi(balance:Balance): BalanceReport = {

    val body = balance.bal.map(btnToApi)

    val deltas = balance.deltas.toSeq
      .map({ case (c, v) =>
        Delta(
          commodity = c.map(_.name),
          delta = scaleFormat(v))
      })
      .sorted(OrderByDelta)

    BalanceReport(balance.metadata, balance.title, body, deltas)
  }
}

class BalanceReporter(val mySettings: BalanceSettings) extends  BalanceReporterLike(mySettings) {

  override val name = mySettings.outputname

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def txtBalanceReport(bal: Balance): Seq[String] = {

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

  protected def jsonBalanceReport(bal: Balance): Json = {
    balanceToApi(bal).asJson
  }

  protected def getBalance(txns: TxnData): Balance = {
    val bf = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    Balance(mySettings.title, txns, bf)
  }

  override
  def jsonReport(txnData: TxnData): Json = {
    val bal = getBalance(txnData)
    jsonBalanceReport(bal)
  }

  override
  def writeReport(formats: Formats, txnData: TxnData): Unit = {

    val bal = getBalance(txnData)

    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() => {
          doRowOutputs(writers, txtBalanceReport(bal))
        }
        case JsonFormat() => {
          doRowOutputs(writers, Seq(jsonBalanceReport(bal).pretty(printer)))
        }
      }
    })
  }
}
