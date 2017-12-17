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
import fi.sn127.tackler.model.{BalanceTreeNode, TxnData}

abstract class BalanceReportLike(cfg: ReportSettings) extends ReportLike(cfg) {

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

  protected implicit val encBalanceTreeNode: Encoder[BalanceTreeNode] = (btn: BalanceTreeNode) => {

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

  protected implicit val encBalance: Encoder[Balance] = (bal: Balance) => {

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
      .sortBy({ case (cOpt, _) =>
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

class BalanceReport(val mySettings: BalanceSettings) extends  BalanceReportLike(mySettings) {

  override val name = mySettings.outputname

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

  protected def jsonBalanceReport(bal: Balance): Json = {
    bal.metadata.fold(
      Json.obj(
        jsonTitle(mySettings.title),
        jsonBalance(bal))
    )({ md =>
      Json.obj(
        jsonTitle(mySettings.title),
        ("metadata", md.asJson()),
        jsonBalance(bal)
      )
    })
  }

  protected def getBalance(txns: TxnData): Balance = {
    val bf = if (mySettings.accounts.isEmpty) {
      AllBalanceAccounts
    } else {
      new BalanceFilterByAccount(mySettings.accounts)
    }

    Balance(None, txns, bf)
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
          doRowOutputs(writers, Seq(jsonBalanceReport(bal).spaces2))
        }
      }
    })
  }
}
