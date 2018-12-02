/*
 * Copyright 2016-2018 sn127.fi
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

import fi.sn127.tackler.api.{Metadata, MetadataItem, TxnFilter}
import fi.sn127.tackler.filter.TxnFilterRoot

/**
 * Transaction data and associated metadata.
 *
 * @param metadata optional metadata about these transactions
 * @param txns transactions
 */
final case class TxnData(metadata: Option[Metadata], txns: Txns) {

  def filter(txnFilter: TxnFilterRoot): TxnData = {

    val filterInfo = Seq(TxnFilter(txnFilter.text("")))
    val mdis: Seq[MetadataItem] = metadata.map(_.metadataItems).getOrElse(Nil) ++ filterInfo

    TxnData(
      Option(Metadata(mdis)),
      txns.filter(txn => {
        txnFilter.filter(txn)
      })
    )
  }
}
