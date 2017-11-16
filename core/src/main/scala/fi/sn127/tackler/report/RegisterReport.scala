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
import fi.sn127.tackler.model._

class RegisterReport(val name: String, val settings: Settings) extends ReportLike {
  private val mySettings = settings.Reports.Register

  def txtRegisterEntry(regEntry: RegisterEntry, regEntryPostings: Seq[RegisterPosting]): Seq[String] = {

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

  def jsonRegisterEntry(registerEntry: RegisterEntry, regEntryPostings: Seq[RegisterPosting]): Option[Json] = {

    if (regEntryPostings.isEmpty) {
      None
    }
    else {
      def foo(c: Option[Commodity]):List[(String, Json)] = {
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


  private def doHeaders(formats: Formats, metadata: Option[Metadata]): Unit = {
    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>
          val reportHeader = List(
            metadata.fold(""){md => md.text()},
            mySettings.title,
            "-" * mySettings.title.length)
          doRowOutputs(writers, reportHeader)

        case JsonFormat() =>
          val header = Seq(metadata.fold(
            Json.obj(
              jsonTitle(mySettings.title))
          )({ md =>
            Json.obj(
              jsonTitle(mySettings.title),
              ("metadata", md.asJson()),
            )
          }).spaces2.dropRight(1) + ",") // stupid hack to remove closing '}' and add ','

          doRowOutputs(writers, header)
      }
    })
  }

  private def doBody(formats: Formats, accounts: Filtering[RegisterPosting], txns: Txns): Unit = {
    def bodyStart(): Unit = {
      formats.foreach({case (format, writers) =>
        format match {
          case TextFormat() =>

          case JsonFormat() =>
            doRowOutputs(writers, List("\"registerRows\" : ["))
        }
      })
    }

    def bodyEnd(): Unit = {
      formats.foreach({case (format, writers) =>
        format match {
          case TextFormat() =>

          case JsonFormat() => {
            // This is silly, json doesn't support trailing comma
            // Other option would be to add full logic
            // to deal with comma between objects and with Txn&Account filtering logic
            // (e.g. ",,{}", "{},,{}", "{},," cases etc.)
            doRowOutputs(writers, List(
              Json.obj(
                ("txn", Json.obj(
                  ("timestamp", Json.Null),
                  ("description", "end-sentry".asJson))),
                ("postings", Seq.empty[String].asJson)).spaces2))

            doRowOutputs(writers, List("]"))
          }
        }
      })
    }


    bodyStart()

    Accumulator.registerStream(txns)({ (regEntry: RegisterEntry) =>

      val regEntryPostings = regEntry._2
        .filter(accounts.predicate)
        .sorted(OrderByRegPosting)


      formats.foreach({ case (format, writers) =>
        format match {
          case TextFormat() => {
            val txtRegEntry = txtRegisterEntry(regEntry, regEntryPostings)
            doRowOutputs(writers, txtRegEntry)
          }

          case JsonFormat() =>
            jsonRegisterEntry(regEntry, regEntryPostings) match {
              case Some(json) => {
                doRowOutputs(writers, List(json.spaces2))
                // See bodyEnd
                doRowOutputs(writers, List(","))
              }
              case None =>
            }
        }
      })
    })

    bodyEnd()
  }

  private def doFooters(formats: Formats): Unit = {
    formats.foreach({case (format, writers) =>
      format match {
        case TextFormat() =>

        case JsonFormat() =>
          val reportFooter = List("}")
          doRowOutputs(writers, reportFooter)
      }
    })
  }

  def doReport(formats: Formats, txns: TxnData): Unit ={
    val rrf = if (mySettings.accounts.isEmpty) {
      AllRegisterPostings
    } else {
      RegisterFilterByAccount(mySettings.accounts)
    }

    doHeaders(formats, txns.metadata)
    doBody(formats, rrf, txns.txns)
    doFooters(formats)
  }
}
