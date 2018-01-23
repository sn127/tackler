/*
 * Copyright 2018 Jani Averbach
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

import io.circe._
import io.circe.generic.semiauto._

final case class BalanceItem(
  accountSum: String,
  accountTreeSum: String,
  account: String,
  commodity: Option[String])

object BalanceItem {
  implicit val decodeBalanceItem: Decoder[BalanceItem] = deriveDecoder[BalanceItem]
  implicit val encodeBalanceItem: Encoder[BalanceItem] = deriveEncoder[BalanceItem]
}

final case class Delta(
  delta: String,
  commodity: Option[String]){

  def compareTo(otherD: Delta): Int = {
    commodity.getOrElse("").compareTo(otherD.commodity.getOrElse(""))
  }
}

object Delta {
  implicit val decodeDelta: Decoder[Delta] = deriveDecoder[Delta]
  implicit val encodeDelta: Encoder[Delta] = deriveEncoder[Delta]
}

object OrderByDelta extends Ordering[Delta] {
  def compare(before: Delta, after: Delta): Int = {
    before.compareTo(after)
  }
}


final case class BalanceReport(
  metadata: Option[Metadata],
  title: String,
  balances: Seq[BalanceItem],
  deltas: Seq[Delta]
)

object BalanceReport {
  implicit val decodeBalanceReport: Decoder[BalanceReport] = deriveDecoder[BalanceReport]
  implicit val encodeBalanceReport: Encoder[BalanceReport] = deriveEncoder[BalanceReport]
}