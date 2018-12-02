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
package fi.sn127.tackler.core

import scala.collection.mutable

import fi.sn127.tackler.model._

object Accumulator {

  def balanceGroups(txns: TxnData, groupOp: (Transaction) => String, balanceFilter: Filtering[BalanceTreeNode]): Seq[Balance] = {
    txns.txns
      .groupBy(groupOp).toSeq
      .sortBy(_._1)
      .par.map({case (groupBy, balGrpTxns) =>
        Balance(groupBy, TxnData(None, balGrpTxns), balanceFilter)
      })
      .filter(bal => !bal.isEmpty)
      .seq
  }

  def registerStream[T](txns: Txns, accounts: Filtering[AccumulatorPosting])(reporter: (RegisterEntry => Seq[T])): Seq[T] = {

    val registerEngine = new mutable.HashMap[String, BigDecimal]()

    txns.flatMap(txn => {
      val registerPostings = txn.posts.map({ p =>
        val newTotal = registerEngine.getOrElse(p.atnKey, BigDecimal(0)) + p.amount
        registerEngine.update(p.atnKey, newTotal)

        AccumulatorPosting(p, newTotal)
      })

      reporter((
        txn,
        registerPostings
          .filter(accounts.predicate)
          .sorted(OrderByAccumulatorPosting)))
    })
  }
}
