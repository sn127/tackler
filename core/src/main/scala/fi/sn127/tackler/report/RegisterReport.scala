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
import fi.sn127.tackler.model._

class RegisterReport(val name: String, val settings: Settings) extends ReportLike {
  private val mySettings = settings.Reports.Register

  def txtRegisterEntry(regEntry: RegisterEntry,
    accounts: Filtering[RegisterPosting])
  : Seq[String] = {

    val txn = regEntry._1
    val registerPostings = regEntry._2

    val indent = " " * 12

    val txtRegTxnHeader: String = txn.txnHeaderToString(indent, TxnTS.isoDate)

    val txtRegPostings = registerPostings
      .filter(accounts.predicate)
      .sorted(OrderByRegPosting)
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

  private def doHeaders(formats: Formats, metadata: Option[Metadata]): Unit = {
    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          val reportHeader = List(
            metadata.fold(""){md => md.text()},
            mySettings.title,
            "-" * mySettings.title.length)
          doRowOutputs(writers, reportHeader)

        case JsonFormat() => ???
      }
    })
  }

  private def doBody(formats: Formats, filter: Filtering[RegisterPosting], txns: Txns): Unit = {

    Accumulator.registerStream(txns)({regEntry: RegisterEntry =>
      formats.foreach({case (format, writers) =>
        format match {
          case TextFormat() =>
            val txtRegEntry = txtRegisterEntry(regEntry, filter)
            doRowOutputs(writers, txtRegEntry)

          case JsonFormat() => ???
        }
      })
    })
  }

  /*
  private def doFooters(): Unit = {

  }
  */

  def doReport(formats: Formats, txns: TxnData): Unit ={
    val rrf = if (mySettings.accounts.isEmpty) {
      AllRegisterPostings
    } else {
      RegisterFilterByAccount(mySettings.accounts)
    }

    doHeaders(formats, txns.metadata)
    doBody(formats, rrf, txns.txns)
    //doFooters()
  }
}
