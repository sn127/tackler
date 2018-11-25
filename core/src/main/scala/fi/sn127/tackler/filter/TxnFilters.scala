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

import cats.implicits._

import fi.sn127.tackler.model.Transaction

final case class TxnFilterTSBegin(begin: ZonedDateTime) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    // why to toInstant? see test: 960cb7e7-b180-4276-a43b-714e53e1789b
    // offsets which cancel each others won't compare correctly otherwise
    begin.toInstant.compareTo(txn.header.timestamp.toInstant) <= 0
  }
}

final case class TxnFilterTSEnd(end: ZonedDateTime) extends TxnFilter {
  override def filter(txn: Transaction): Boolean = {
    txn.header.timestamp.toInstant.isBefore(end.toInstant)
  }
}

sealed abstract class TxnFilterRegex(regex: String) extends TxnFilter {
  val rgx = java.util.regex.Pattern.compile(regex)
}

final case class TxnFilterDescription(regex: String) extends TxnFilterRegex(regex) {

  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.description.getOrElse("")).matches
  }
}

final case class TxnFilterCode(regex: String) extends TxnFilterRegex(regex) {

  override def filter(txn: Transaction): Boolean = {
    rgx.matcher(txn.header.code.getOrElse("")).matches
  }
}

final case class TxnFilterTxnUUID(uuid: UUID) extends TxnFilter {

  override def filter(txn: Transaction): Boolean = {
    txn.header.uuid.exists(_ === uuid)
  }
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

