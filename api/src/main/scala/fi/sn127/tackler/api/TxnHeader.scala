/*
 * Copyright 2016-2018 SN127.fi
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

import cats.implicits._

import io.circe.java8.time._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


final case class TxnHeader(
  timestamp: ZonedDateTime,
  code: Option[String],
  description: Option[String],
  uuid: Option[UUID],
  comments: Option[List[String]]
) {

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
  def compareTo(otherTxn: TxnHeader): Int = {
    // TODO: Write offset based test to expose bug in compare without toInstant
    val dateCmp = timestamp.toInstant.compareTo(otherTxn.timestamp.toInstant)
    if (dateCmp =!= 0) {
      dateCmp
    } else {
      val codeCmp = code.getOrElse("").compareTo(otherTxn.code.getOrElse(""))
      if (0 =!= codeCmp) {
        codeCmp
      } else {
        val descCmp = description.getOrElse("").compareTo(otherTxn.description.getOrElse(""))
        if (0 =!= descCmp) {
          descCmp
        } else {
          val uuidThis = uuid.map(_.toString).getOrElse("")
          val uuidOther = otherTxn.uuid.map(_.toString).getOrElse("")
          val uuidCmp = uuidThis.compareTo(uuidOther)
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

    tsFormatter(timestamp) + codeStr.getOrElse(" ") + description.getOrElse("") + "\n" +
      uuidStr.getOrElse("") +
      commentsStr.getOrElse("")
  }
}


object TxnHeader{
  implicit val decodeTxnHeader: Decoder[TxnHeader] = deriveDecoder
  implicit val encodeTxnHeader: Encoder[TxnHeader] = deriveEncoder
}
