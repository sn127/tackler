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

import fi.sn127.tackler.api.Metadata
import fi.sn127.tackler.filter.TxnFilter

/**
 * Transaction data and associated metadata.
 *
 * @param metadata for these transactions
 * @param txns transactions
 */
final case class TxnData(metadata: Option[Metadata], txns: Txns) {

  def filter(txnFilter: TxnFilter): TxnData = {
    TxnData(
      // TODO: create new instance of metadata based on filter
      // TODO: handle multiple rounds of filtering (with correct info in metadata)
      // TODO: e.g. multiple rounds == AND filter between rounds
      metadata,
      txns.filter(txn => {
        txnFilter.filter(txn)
      })
    )
  }
}
