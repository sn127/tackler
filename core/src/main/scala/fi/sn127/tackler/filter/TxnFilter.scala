/*
 * Copyright 2018 sn127.fi
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
package fi.sn127.tackler.filter

import java.time.ZonedDateTime
import java.util.UUID

import fi.sn127.tackler.model.{Transaction, TxnTS}
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


sealed trait TxnFilter {
  /**
   * Test if txn satisfies a filter.
   *
   * @param txn to be tested against filter
   * @return true if satisfies a predicate, false otherwise
   */
  def filter(txn: Transaction): Boolean

  def text(indent: String): String = indent
}
object TxnFilter {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilter: Decoder[TxnFilter] = deriveDecoder[TxnFilter]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilter: Encoder[TxnFilter] = deriveEncoder[TxnFilter]
}

final case class TxnFilterRoot(txnFilter: TxnFilter) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txnFilter.filter(txn)
  }

  override def text(indent: String): String = {
    val myIndent = indent + "  "
    indent + "Filter:" + "\n" +
      txnFilter.text(myIndent) + "\n"
  }
}
object TxnFilterRoot {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilterRoot: Decoder[TxnFilterRoot] = deriveDecoder[TxnFilterRoot]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilterRoot: Encoder[TxnFilterRoot] = deriveEncoder[TxnFilterRoot]
}

final class TxnFilterFalse() extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    false
  }
}

final class TxnFilterTrue() extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    true
  }
}



sealed trait TxnFilterList extends TxnFilter {
  val txnFilters: Seq[TxnFilter]
  val opTxt: String

  override def text(indent: String): String = {
    val myIndent = indent + "  "
    indent +  opTxt + "\n" + txnFilters.map(f => f.text(myIndent)).mkString("\n")
  }
}

final case class TxnFilterListAND(txnFilters: Seq[TxnFilter]) extends TxnFilterList() {
  val opTxt = "AND"

  override def filter(txn: Transaction): Boolean = {
    txnFilters.forall(f => f.filter(txn))
  }
}


sealed case class TxnFilterListOR(txnFilters: Seq[TxnFilter]) extends TxnFilterList {
  val opTxt = "OR"

  override def filter(txn: Transaction): Boolean = {
    txnFilters.exists(f => f.filter(txn) === true )
  }
}

sealed case class TxnFilterNodeNOT(txnFilter: TxnFilter) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txnFilter.filter(txn) === false
  }

  override def text(indent: String): String = {
    val myIndent = indent + "  "
    indent + "NOT\n" + txnFilter.text(myIndent)
  }

}

sealed abstract class TxnFilterTxnTS(ts: ZonedDateTime) extends TxnFilter {
  val opTxt: String

  override def text(indent: String): String = {
    indent +  "Txn TS: " + opTxt + " " + TxnTS.isoZonedTS(ts)
  }
}

final case class TxnFilterTxnTSBegin(begin: ZonedDateTime) extends TxnFilterTxnTS(begin) {
  val opTxt = "begin"

  override def filter(txn: Transaction): Boolean = {
    // why to toInstant? see test: 960cb7e7-b180-4276-a43b-714e53e1789b
    // offsets which cancel each others won't compare correctly otherwise
    begin.toInstant.compareTo(txn.header.timestamp.toInstant) <= 0
  }
}

final case class TxnFilterTxnTSEnd(end: ZonedDateTime) extends TxnFilterTxnTS(end) {
  val opTxt = "end  "

  override def filter(txn: Transaction): Boolean = {
    txn.header.timestamp.toInstant.isBefore(end.toInstant)
  }
}

sealed abstract class TxnFilterRegex(regex: String) extends TxnFilter {
  val rgx = java.util.regex.Pattern.compile(regex)

  val target: String

  override def text(indent: String): String = {
    indent +  target + ": " + "\"" + s"${regex}" + "\""
  }
}


final case class TxnFilterTxnDescription(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Description"

  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.description.getOrElse("")).matches
  }
}

final case class TxnFilterTxnCode(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Code"

  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.code.getOrElse("")).matches
  }
}


final case class TxnFilterTxnUUID(uuid: UUID) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txn.header.uuid.exists(_ === uuid)
  }

  override def text(indent: String): String = {
    indent +  "Txn UUID: " + uuid.toString
  }
}

final case class TxnFilterTxnComments(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Comments"

  override def filter(txn: Transaction): Boolean = {
    txn.header.comments.map(cmts => {
      cmts.exists(c => rgx.matcher(c).matches())
    }) match {
      case Some(b) => b
      case None => false
    }
  }
}
/*
 *
 * TXN: POSTINGS
 *
 */
final case class TxnFilterPostingAccount(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Account"

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => rgx.matcher(p.acctn.account).matches())
  }
}

final case class TxnFilterPostingComment(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Comment"

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      p.comment.exists(rgx.matcher(_).matches())
    })
  }
}

sealed abstract class TxnFilterPosting(regex: String, amount: BigDecimal) extends TxnFilterRegex(regex) {
  val opTxt: String

  override def text(indent: String): String = {
    val myIndent = indent + "  "
    indent +  target + "\n" +
      myIndent + "account: " + "\"" + s"${regex}" + "\"" +"\n" +
      myIndent + "amount " + opTxt + " " + amount.toString
  }
}

final case class TxnFilterPostingAmountEqual(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = "=="

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) === 0
    })
  }
}


final case class TxnFilterPostingAmountLess(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = "<"

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) < 0
    })
  }
}

final case class TxnFilterPostingAmountGreater(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = ">"

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) > 0
    })
  }
}


final case class TxnFilterPostingCommodity(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Commodity"

  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      p.acctn.commodity.exists(c => rgx.matcher(c.name).matches())
    })
  }
}
