package fi.sn127.tackler.report

import fi.sn127.tackler.core._
import fi.sn127.tackler.model.{TxnTS, Txns}

class EquityReport(val settings: Settings) extends ExportLike {
  private val mySettings = settings.Reports.Equity

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  def txnEquity(txns: Txns): Seq[String] = {

    val bf = if (mySettings.accounts.isEmpty) {
      new BalanceFilterNonZero()
    } else {
      new BalanceFilterNonZeroByAccount(mySettings.accounts)
    }
    val bal = Balance("", txns, bf)

    if (bal.isEmpty) {
      Nil
    } else {
      val lastTxn = txns.last
      val eqTxnHeader = TxnTS.isoZonedTS(lastTxn.date) + " " + lastTxn.uuid.map(u => "Equity: last txn (uuid): " + u.toString).getOrElse("Equity")

      List(eqTxnHeader) ++
        bal.bal.map(acc => {
          " " + acc.acctn.account + "  " + acc.accountSum.toString()
        }) ++
        List(" " + "Equity:Balance")
    }
  }

  def doExport(writer: Writer, txns: Txns): Unit = {

    val txtEqReport = txnEquity(txns)
    txnWriter(writer, txtEqReport)
  }
}
