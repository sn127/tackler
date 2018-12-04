package fi.sn127.tackler.filter

import fi.sn127.tackler.model.Transaction

trait CanTxnFilter[A] {
  def filter(tf: A, txn: Transaction): Boolean
}
