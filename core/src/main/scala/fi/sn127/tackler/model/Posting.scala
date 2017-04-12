package fi.sn127.tackler.model
import cats.implicits._

import fi.sn127.tackler.core.TxnException

final case class Posting(
  acctn: AccountTreeNode,
  amount: BigDecimal,
  comment: Option[String]) {

  if (amount.compareTo(BigDecimal(0)) === 0) {
    throw new TxnException("Zero sum postings are not allowed (is it typo?): " + acctn.account)
  }

  def account: String = acctn.account

  override
  def toString: String = {
    val missingSign = if (amount < 0) "" else " "
    "   " + acctn.toString + "  " +
      missingSign + amount.toString() +
      comment.map(c => " ; " + c).getOrElse("")
  }
}

object Posting {
  def sumPosts(posts: Posts): BigDecimal = {
    posts.foldLeft(BigDecimal(0))(_ + _.amount)
  }
}
