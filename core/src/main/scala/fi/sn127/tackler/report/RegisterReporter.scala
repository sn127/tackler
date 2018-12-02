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

import io.circe.Json
import io.circe.syntax._

import fi.sn127.tackler.api.{RegisterPosting, RegisterReport, RegisterTxn}
import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{RegisterEntry, _}

class RegisterReporter(val mySettings: RegisterSettings) extends ReportLike(mySettings) {

  override val name = mySettings.outputname

  protected def txtRegisterEntry(regEntry: RegisterEntry, regEntryPostings: Seq[AccumulatorPosting]): Seq[String] = {

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

  protected def jsonRegisterEntry(registerEntry: RegisterEntry, regEntryPostings: Seq[AccumulatorPosting]): Seq[RegisterTxn] = {

    if (regEntryPostings.isEmpty) {
      Seq.empty[RegisterTxn]
    }
    else {
      val txn = registerEntry._1

      val reportPostings = regEntryPostings
        .map(regPosting => {
          RegisterPosting(
            account = regPosting.account,
            amount = scaleFormat(regPosting.amount),
            runningTotal = scaleFormat(regPosting.runningTotal),
            commodity = regPosting.commodity.map(_.name)
          )
        })

      List(RegisterTxn(txn.header, reportPostings))
    }
  }

  protected def txtRegisterReport(accounts: Filtering[AccumulatorPosting], txns: TxnData): Seq[String] = {
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

  protected def doBody(accounts: Filtering[AccumulatorPosting], txns: Txns): Seq[RegisterTxn] = {

    val a = Accumulator.registerStream[RegisterTxn](txns, accounts)({ (regEntry: RegisterEntry) =>
      val regEntryPostings = regEntry._2
      jsonRegisterEntry(regEntry, regEntryPostings)
    })

    a
  }

  protected def jsonRegisterReport(accounts: Filtering[AccumulatorPosting], txns: TxnData): Json = {

    RegisterReport(txns.metadata, mySettings.title, doBody(accounts, txns.txns)).asJson
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
          doRowOutputs(writers, Seq(jsonRegisterReport(rrf, txns).pretty(printer)))
        }
      }
    })
  }
}
