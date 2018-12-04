/*
 * Copyright 2018 SN127.fi
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
package fi.sn127.tackler.api

import java.time.ZonedDateTime
import java.util.UUID

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// Circe with java8 time: https://github.com/circe/circe/issues/378
import io.circe.java8.time.decodeZonedDateTime
import io.circe.java8.time.encodeZonedDateTime

sealed trait TxnFilter {
  def text(indent: String): String = indent
}
object TxnFilter {
  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val decodeTxnFilter: Decoder[TxnFilter] = deriveDecoder[TxnFilter]

  @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
  implicit val encodeTxnFilter: Encoder[TxnFilter] = deriveEncoder[TxnFilter]
}

final case class TxnFilterRoot(txnFilter: TxnFilter) extends TxnFilter {
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
}

final class TxnFilterTrue() extends TxnFilter {
}


sealed trait TxnFilters extends TxnFilter {
  val txnFilters: Seq[TxnFilter]
  val opTxt: String

  override def text(indent: String): String = {
    val myIndent = indent + "  "
    indent +  opTxt + "\n" + txnFilters.map(f => f.text(myIndent)).mkString("\n")
  }
}

final case class TxnFiltersAND(txnFilters: Seq[TxnFilter]) extends TxnFilters() {
  val opTxt = "AND"

}

sealed case class TxnFiltersOR(txnFilters: Seq[TxnFilter]) extends TxnFilters {
  val opTxt = "OR"
}

sealed case class TxnFilterNOT(txnFilter: TxnFilter) extends TxnFilter {
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

// Circe with java8 time: https://github.com/circe/circe/issues/378
final case class TxnFilterTxnTSBegin(begin: ZonedDateTime) extends TxnFilterTxnTS(begin) {
  val opTxt = "begin"
}

// Circe with java8 time: https://github.com/circe/circe/issues/378
final case class TxnFilterTxnTSEnd(end: ZonedDateTime) extends TxnFilterTxnTS(end) {
  val opTxt = "end  "
}

// Circe with java8 time: https://github.com/circe/circe/issues/378
sealed abstract class TxnFilterRegex(regex: String) extends TxnFilter {
  val rgx = java.util.regex.Pattern.compile(regex)

  val target: String

  override def text(indent: String): String = {
    indent +  target + ": " + "\"" + s"${regex}" + "\""
  }
}


final case class TxnFilterTxnDescription(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Description"

}

final case class TxnFilterTxnCode(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Code"
}


final case class TxnFilterTxnUUID(uuid: UUID) extends TxnFilter {

  override def text(indent: String): String = {
    indent +  "Txn UUID: " + uuid.toString
  }
}

final case class TxnFilterTxnComments(regex: String) extends TxnFilterRegex(regex) {
  val target = "Txn Comments"
}

/*
 *
 * TXN: POSTINGS
 *
 */
final case class TxnFilterPostingAccount(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Account"

}

final case class TxnFilterPostingComment(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Comment"

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

}


final case class TxnFilterPostingAmountLess(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = "<"

}

final case class TxnFilterPostingAmountGreater(regex: String, amount: BigDecimal) extends TxnFilterPosting(regex, amount) {
  val target = "Posting Amount"
  val opTxt: String = ">"

}


final case class TxnFilterPostingCommodity(regex: String) extends TxnFilterRegex(regex) {
  val target = "Posting Commodity"

}
