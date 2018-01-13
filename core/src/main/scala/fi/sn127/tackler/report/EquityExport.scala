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
import cats.implicits._
import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{TxnData, TxnTS, Txns}

class EquityExport(val settings: Settings) extends ExportLike {
  private val mySettings = settings.Exports.Equity

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  def txnEquity(txns: Txns): Seq[String] = {

    val bf = if (mySettings.accounts.isEmpty) {
      new BalanceFilterNonZero()
    } else {
      new BalanceFilterNonZeroByAccount(mySettings.accounts)
    }
    val bal = Balance("", TxnData(None, txns), bf)

    if (bal.isEmpty) {
      Nil
    } else {
      val lastTxn = txns.last
      val eqTxnHeader = TxnTS.isoZonedTS(lastTxn.date) + " " + lastTxn.uuid.map(u => "Equity: last txn (uuid): " + u.toString).getOrElse("Equity")

      bal.bal.groupBy(b => b.acctn.commStr).flatMap({ case (_, bs) =>
        val eqBalRow = if (bs.map(b => b.accountSum).sum === 0.0) {
          Nil
        } else {
          List(" " + "Equity:Balance")
        }

        List(eqTxnHeader) ++
          bs.map(acc => {
            " " + acc.acctn.account + "  " + acc.accountSum.toString() + acc.acctn.commodity.map(c => " " + c.name).getOrElse("")
          }) ++ eqBalRow ++ List("")

      }).toSeq
    }
  }

  def writeExport(writer: Writer, txns: Txns): Unit = {

    val txtEqReport = txnEquity(txns)
    doRowOutput(writer, txtEqReport)
  }
}
