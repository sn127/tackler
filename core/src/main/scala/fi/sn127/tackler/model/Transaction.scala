package fi.sn127.tackler.model
import java.time.ZonedDateTime
import java.util.UUID

import cats.implicits._

import fi.sn127.tackler.core.TxnException

object OrderByTxn extends Ordering[Transaction] {
  def compare(before: Transaction, after: Transaction): Int = {
    before.compareTo(after)
  }
}

final case class Transaction(
  date: ZonedDateTime,
  code: Option[String],
  desc: Option[String],
  uuid: Option[UUID],
  comments: Option[List[String]],
  posts: Posts) {

  if (BigDecimal(0).compareTo(Posting.sumPosts(posts)) =!= 0) {
    throw new TxnException("TXN postings do not zero: " + Posting.sumPosts(posts).toString())
  }

  /**
   * Txn sorting logic.
   *
   * Input order of Txn is not significant, so there should
   * be a stable way to sort transactions.
   *
   * Txn components are used in following order to find sort order
   * (in case of previous components have been same):
   *  date, code, description, uuid
   *
   * If fully deterministic sort order is needed, then transactions must have
   * uuid field.
   *
   * @param otherTxn to be compared to this Txn
   * @return 0 if the argument txn is equal to this Txn.
   *         less than 0, if this Txn is (before in sorted set)
   *         greater than 0, if this Txn is after (in sorted set)
   */
  def compareTo(otherTxn: Transaction): Int = {
    val dateCmp = date.compareTo(otherTxn.date)
    if (dateCmp =!= 0) {
      dateCmp
    } else {
      val codeCmp = code.getOrElse("").compareTo(otherTxn.code.getOrElse(""))
      if (0 =!= codeCmp) {
        codeCmp
      } else {
        val descCmp = desc.getOrElse("").compareTo(otherTxn.desc.getOrElse(""))
        if (0 =!= descCmp) {
          descCmp
        } else {
          val uuidCmp = uuid.getOrElse("").toString.compareTo(otherTxn.uuid.getOrElse("").toString)
          uuidCmp
        }
      }
    }
  }

  def txnHeaderToString(indent: String, tsFormatter: (ZonedDateTime => String)): String = {
    val codeStr = code.map(c => " (" + c + ") ")

    val uuidStr = uuid.map(u => indent + ";:uuid: " + u.toString + "\n")
    val commentsStr = comments.map(cs =>
      cs.map(c => indent + "; " + c + "\n").mkString
    )

    tsFormatter(date) + codeStr.getOrElse(" ") + desc.getOrElse("") + "\n" +
      uuidStr.getOrElse("") +
      commentsStr.getOrElse("")
  }

  override def toString: String = {
    val indent = " " * 3

    txnHeaderToString(indent, TxnTS.isoZonedTS) +
      posts.map(p => p.toString).mkString("\n") + "\n"
  }
}
