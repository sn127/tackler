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

import fi.sn127.tackler.model.Transaction
import cats.implicits._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait TxnFilter {
  def filter(txn: Transaction): Boolean
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



sealed trait TxnFilterTree extends TxnFilter {
  val txnFilters: Seq[TxnFilter]
}
object TxnFilterTree {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilterTree: Decoder[TxnFilterTree] = deriveDecoder[TxnFilterTree]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilterTree: Encoder[TxnFilterTree] = deriveEncoder[TxnFilterTree]
}


final case class TxnFilterTreeAND(txnFilters: Seq[TxnFilter]) extends TxnFilterTree {
  override def filter(txn: Transaction): Boolean = {
    txnFilters.forall(f => f.filter(txn))
  }
}
object TxnFilterTreeAND {
  implicit val decodeTxnFilterTreeAND: Decoder[TxnFilterTreeAND] = deriveDecoder[TxnFilterTreeAND]
  implicit val encodeTxnFilterTreeAND: Encoder[TxnFilterTreeAND] = deriveEncoder[TxnFilterTreeAND]
}


sealed case class TxnFilterTreeOR(txnFilters: Seq[TxnFilter]) extends TxnFilterTree {
  override def filter(txn: Transaction): Boolean = {
    txnFilters.exists(f => f.filter(txn) === true )
  }
}
object TxnFilterTreeOR {
  implicit val decodeTxnFilterTreeOR: Decoder[TxnFilterTreeOR] = deriveDecoder[TxnFilterTreeOR]
  implicit val encodeTxnFilterTreeOR: Encoder[TxnFilterTreeOR] = deriveEncoder[TxnFilterTreeOR]
}


sealed case class TxnFilterNodeNOT(txnFilter: TxnFilter) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txnFilter.filter(txn) === false
  }
}
object TxnFilterNodeNOT {
  implicit val decodeTxnFilterNodeNOT: Decoder[TxnFilterNodeNOT] = deriveDecoder[TxnFilterNodeNOT]
  implicit val encodeTxnFilterNodeNOT: Encoder[TxnFilterNodeNOT] = deriveEncoder[TxnFilterNodeNOT]
}


final case class TxnFilterTSBegin(begin: ZonedDateTime) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    // why to toInstant? see test: 960cb7e7-b180-4276-a43b-714e53e1789b
    // offsets which cancel each others won't compare correctly otherwise
    begin.toInstant.compareTo(txn.header.timestamp.toInstant) <= 0
  }
}
object TxnFilterTSBegin {
  implicit val decodeTxnFilterTSBegin: Decoder[TxnFilterTSBegin] = deriveDecoder[TxnFilterTSBegin]
  implicit val encodeTxnFilterTSBegin: Encoder[TxnFilterTSBegin] = deriveEncoder[TxnFilterTSBegin]
}


final case class TxnFilterTSEnd(end: ZonedDateTime) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txn.header.timestamp.toInstant.isBefore(end.toInstant)
  }
}
object TxnFilterTSEnd {
  implicit val decodeTxnFilterTSEnd: Decoder[TxnFilterTSEnd] = deriveDecoder[TxnFilterTSEnd]
  implicit val encodeTxnFilterTSEnd: Encoder[TxnFilterTSEnd] = deriveEncoder[TxnFilterTSEnd]
}


sealed abstract class TxnFilterRegex(regex: String) extends TxnFilter {
  val rgx = java.util.regex.Pattern.compile(regex)
}


final case class TxnFilterDescription(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.description.getOrElse("")).matches
  }
}

object TxnFilterDescription {
  implicit val decodeTxnFilterDescription: Decoder[TxnFilterDescription] = deriveDecoder[TxnFilterDescription]
  implicit val encodeTxnFilterDescription: Encoder[TxnFilterDescription] = deriveEncoder[TxnFilterDescription]
}


final case class TxnFilterCode(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.code.getOrElse("")).matches
  }
}

object TxnFilterCode {
  implicit val decodeTxnFilterCode: Decoder[TxnFilterCode] = deriveDecoder[TxnFilterCode]
  implicit val encodeTxnFilterCode: Encoder[TxnFilterCode] = deriveEncoder[TxnFilterCode]
}



final case class TxnFilterTxnUUID(uuid: UUID) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txn.header.uuid.exists(_ === uuid)
  }
}

object TxnFilterTxnUUID {
  implicit val decodeTxnFilterTxnUUID: Decoder[TxnFilterTxnUUID] = deriveDecoder[TxnFilterTxnUUID]
  implicit val encodeTxnFilterTxnUUID: Encoder[TxnFilterTxnUUID] = deriveEncoder[TxnFilterTxnUUID]
}


final case class TxnFilterTxnComments(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.header.comments.map(cmts => {
      cmts.exists(c => rgx.matcher(c).matches())
    }) match {
      case Some(b) => b
      case None => false
    }
  }
}

object TxnFilterTxnComments {
  implicit val decodeTxnFilterTxnComments: Decoder[TxnFilterTxnComments] = deriveDecoder[TxnFilterTxnComments]
  implicit val encodeTxnFilterTxnComments: Encoder[TxnFilterTxnComments] = deriveEncoder[TxnFilterTxnComments]
}

/*
 *
 * TXN: POSTINGS
 *
 */
final case class TxnFilterPostingAccount(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => rgx.matcher(p.acctn.account).matches())
  }
}
object TxnFilterPostingAccount {
  implicit val decodeTxnFilterPostingAccount: Decoder[TxnFilterPostingAccount] = deriveDecoder[TxnFilterPostingAccount]
  implicit val encodeTxnFilterPostingAccount: Encoder[TxnFilterPostingAccount] = deriveEncoder[TxnFilterPostingAccount]
}


final case class TxnFilterPostingComments(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      p.comment.exists(rgx.matcher(_).matches())
    })
  }
}

object TxnFilterPostingComments {
  implicit val decodeTxnFilterPostingComments: Decoder[TxnFilterPostingComments] = deriveDecoder[TxnFilterPostingComments]
  implicit val encodeTxnFilterPostingComments: Encoder[TxnFilterPostingComments] = deriveEncoder[TxnFilterPostingComments]
}


final case class TxnFilterPostingAmountEqual(regex: String, amount: BigDecimal) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) === 0
    })
  }
}

object TxnFilterPostingAmountEqual {
  implicit val decodeTxnFilterPostingAmountEqual: Decoder[TxnFilterPostingAmountEqual] = deriveDecoder[TxnFilterPostingAmountEqual]
  implicit val encodeTxnFilterPostingAmountEqual: Encoder[TxnFilterPostingAmountEqual] = deriveEncoder[TxnFilterPostingAmountEqual]
}


final case class TxnFilterPostingAmountLess(regex: String, amount: BigDecimal) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) < 0
    })
  }
}

object TxnFilterPostingAmountLess {
  implicit val decodeTxnFilterPostingAmountLess: Decoder[TxnFilterPostingAmountLess] = deriveDecoder[TxnFilterPostingAmountLess]
  implicit val encodeTxnFilterPostingAmountLess: Encoder[TxnFilterPostingAmountLess] = deriveEncoder[TxnFilterPostingAmountLess]
}

final case class TxnFilterPostingAmountGreater(regex: String, amount: BigDecimal) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      rgx.matcher(p.acctn.account).matches() &&
        p.amount.compare(amount) > 0
    })
  }
}

object TxnFilterPostingAmountGreater {
  implicit val decodeTxnFilterPostingAmountGreater: Decoder[TxnFilterPostingAmountGreater] = deriveDecoder[TxnFilterPostingAmountGreater]
  implicit val encodeTxnFilterPostingAmountGreater: Encoder[TxnFilterPostingAmountGreater] = deriveEncoder[TxnFilterPostingAmountGreater]
}


final case class TxnFilterPostingCommodity(regex: String) extends TxnFilterRegex(regex) {
  override def filter(txn: Transaction): Boolean = {
    txn.posts.exists(p => {
      p.acctn.commodity.exists(c => rgx.matcher(c.name).matches())
    })
  }
}

object TxnFilterPostingCommodity {
  implicit val decodeTxnFilterPostingCommodity: Decoder[TxnFilterPostingCommodity] = deriveDecoder[TxnFilterPostingCommodity]
  implicit val encodeTxnFilterPostingCommodity: Encoder[TxnFilterPostingCommodity] = deriveEncoder[TxnFilterPostingCommodity]
}
