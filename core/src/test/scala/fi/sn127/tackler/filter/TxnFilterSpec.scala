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

import org.scalatest.FlatSpec

import fi.sn127.tackler.model.TxnData

abstract class TxnFilterSpec extends FlatSpec {
  def checkUUID(txnData: TxnData, uuid: String) = {
    txnData.txns.exists(txn => txn.header.uuid.map(u => u.toString).getOrElse("") === uuid)
  }
}
