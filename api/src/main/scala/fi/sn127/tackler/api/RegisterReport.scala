/*
 * Copyright 2016-2018 Jani Averbach
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


final case class RegisterPosting(
  account: String,
  amount: String,
  runningTotal: String,
  commodity: Option[String]
)

object RegisterPosting {
  implicit val decodeRegisterPosting: Decoder[RegisterPosting] = deriveDecoder
  implicit val encodeRegisterPosting: Encoder[RegisterPosting] = deriveEncoder
}


final case class RegisterTxn(
  txn: TxnHeader,
  postings: Seq[RegisterPosting]
)

object RegisterTxn {
  implicit val decodeRegisterTxn: Decoder[RegisterTxn] = deriveDecoder
  implicit val encodeRegisterTxn: Encoder[RegisterTxn] = deriveEncoder
}


final case class RegisterReport(
  metadata: Option[Metadata],
  title: String,
  transactions: Seq[RegisterTxn]
)

object RegisterReport {
  implicit val decodeRegisterReport: Decoder[RegisterReport] = deriveDecoder
  implicit val encodeRegisterReport: Encoder[RegisterReport] = deriveEncoder
}
