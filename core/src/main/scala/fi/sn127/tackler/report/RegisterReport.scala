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
import fi.sn127.tackler.model.{RegisterEntry, _}

class RegisterReport(val mySettings: RegisterSettings) extends ReportLike(mySettings) {

  override val name = mySettings.outputname

  protected def txtRegisterEntry(regEntry: RegisterEntry, regEntryPostings: Seq[RegisterPosting]): Seq[String] = {

    val txn = regEntry._1

    val indent = " " * 12

    val txtRegTxnHeader: String = txn.txnHeaderToString(indent, TxnTS.isoDate)

    val txtRegPostings = regEntryPostings
      .map(regPosting => {
        indent + "%-33s".format(regPosting.account) +
          fillFormat(18, regPosting.amount) + " " + fillFormat(18, regPosting.runningTotal) +
          regPosting.commodity.map(c => " " + c.name).getOrElse("")
      })

    if (txtRegPostings.nonEmpty) {
      List(txtRegTxnHeader + txtRegPostings.mkString("\n"))
    } else {
      Nil
    }
  }

  protected def jsonRegisterEntry(registerEntry: RegisterEntry, regEntryPostings: Seq[RegisterPosting]): Option[Json] = {

    if (regEntryPostings.isEmpty) {
      None
    }
    else {
      def foo(c: Option[Commodity]): List[(String, Json)] = {
        c.fold(
          Nil: List[(String, Json)]
        )(commodity =>
          List(("commodity", commodity.name.asJson))
        )
      }

      val txn = registerEntry._1

      val jsonPostings = regEntryPostings
        .map(regPosting => {
          val js = List(
            ("account", regPosting.account.asJson),
            ("amount", scaleFormat(regPosting.amount).asJson),
            ("runningTotal", scaleFormat(regPosting.runningTotal).asJson)
          ) ++ foo(regPosting.commodity)
          Json.obj(js: _*)
        })

      Some(Json.obj(
        ("txn", txn.txnHeaderToJson(TxnTS.isoDate)),
        ("postings", jsonPostings.asJson)
      ))
    }
  }

  protected def txtRegisterReport(accounts: Filtering[RegisterPosting], txns: TxnData): Seq[String] = {
    val header = List(
      txns.metadata.fold("") { md => md.text() },
      mySettings.title,
      "-" * mySettings.title.length)


    val body = Accumulator.registerStream[String](txns.txns, accounts)({ (regEntry: RegisterEntry) =>
      val regEntryPostings = regEntry._2
      val txtRegEntry = txtRegisterEntry(regEntry, regEntryPostings)

      txtRegEntry

    })
    if (body.isEmpty) {
      header
    } else {
      header ++ body
    }
  }

  protected def doBody(accounts: Filtering[RegisterPosting], txns: Txns): Json = {

    val a = Accumulator.registerStream[Json](txns, accounts)({ (regEntry: RegisterEntry) =>

      val regEntryPostings = regEntry._2

      jsonRegisterEntry(regEntry, regEntryPostings) match {
        case Some(json) => { List(json) }

        case None => Seq.empty[Json]
      }
    })

    a.asJson
  }

  protected def jsonRegisterReport(accounts: Filtering[RegisterPosting], txns: TxnData): Json = {
    txns.metadata.fold(
      Json.obj(
        jsonTitle(mySettings.title),
        ("registerRows", doBody(accounts, txns.txns)))
    )({ md =>
      Json.obj(
        jsonTitle(mySettings.title),
        ("metadata", md.asJson()),
        ("registerRows", doBody(accounts, txns.txns)))
    })
  }

  protected def getFilters() = {
    if (mySettings.accounts.isEmpty) {
      AllRegisterPostings
    } else {
      RegisterFilterByAccount(mySettings.accounts)
    }
  }

  override
  def jsonReport(txnData: TxnData): Json = {
    jsonRegisterReport(getFilters(), txnData)
  }

  override
  def writeReport(formats: Formats, txns: TxnData): Unit = {
    val rrf = getFilters()

    formats.foreach({ case (format, writers) =>
      format match {
        case TextFormat() => {
          doRowOutputs(writers, txtRegisterReport(rrf, txns))
        }
        case JsonFormat() => {
          doRowOutputs(writers, Seq(jsonRegisterReport(rrf, txns).spaces2))
        }
      }
    })
  }
}
