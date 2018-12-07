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
package fi.sn127.tackler.model

import fi.sn127.tackler.api.{Metadata, MetadataItem, TxnFilterDefinition, TxnFilterRoot}
import fi.sn127.tackler.filter._

/**
 * Transaction data and associated metadata.
 *
 * @param metadata optional metadata about these transactions
 * @param txns     transactions
 */
final case class TxnData(metadata: Option[Metadata], txns: Txns) {

  /**
   * Filter this TxnData based on provided transaction filter.
   * Resulting txn sequence will contain only those transaction
   * which are selected by filter.
   *
   * @param txnFilter is transaction filter definition
   * @return new [[TxnData]] which contains filtered txn sequence.
   *         Metadata of returned [[TxnData]] is augmented with TxnFilterDefinition item
   *         which contains information about used filter.
   */
  def filter(txnFilter: TxnFilterRoot): TxnData = {

    val filterInfo = Seq(TxnFilterDefinition(txnFilter))
    val mdis: Seq[MetadataItem] = metadata.map(_.metadataItems).getOrElse(Nil) ++ filterInfo

    TxnData(
      Option(Metadata(mdis)),
      txns.filter(txn => {
        txnFilter.filter(txn)
      })
    )
  }
}
