package fi.sn127.tackler.report

import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.model.Txns

trait ExportLike {
  val settings: Settings

  protected def txnWriter(writer: Writer, rows: Seq[String]): Unit = {
    rows.foreach(row => {
      writer.write(row)
      writer.write("\n")
    })
    writer.flush()
  }

  def doExport(writer: Writer, txns: Txns): Unit
}
