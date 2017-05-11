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

  if (BigDecimal(0).compareTo(Posting.txnSum(posts)) =!= 0) {
    throw new TxnException("TXN postings do not zero: " + Posting.txnSum(posts).toString())
  }

  /**
   * Txn sorting logic.
   *
   * Input order of Txn can not be mandated, so there should
   * be a stable way to sort transactions.
   *
   * Txn components are used in following order to find sort order
   * (in case of previous components have produced "same" sort order):
   *  timestamp, code, description, uuid
   *
   * If fully deterministic and safe "distributed txn source"-proof
   * sort order is needed, then transactions must have UUIDs.
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

  /**
   * Get header part of txn as string.
   *
   * @param indent indent string for meta and comment parts
   * @param tsFormatter timestamp formatter
   * @return new line terminated header of txn
   */
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
      posts.map(p => indent + p.toString).mkString("\n") + "\n"
  }
}
