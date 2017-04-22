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
package fi.sn127.tackler.core

import scala.collection.mutable

import fi.sn127.tackler.model.{BalanceTreeNode, RegisterEntry, RegisterPosting, Transaction, Txns, TxnData}

object Accumulator {

  def balanceGroups(txns: TxnData, groupOp: (Transaction) => String, balanceFilter: Filtering[BalanceTreeNode]): Seq[Balance] = {
    txns.txns
      .groupBy(groupOp).toSeq
      .sortBy(_._1)
      .par.map({case (groupBy, balGrpTxns) =>
        Balance(groupBy, TxnData(None, balGrpTxns), balanceFilter)
      }).toList
  }

  def registerStream(txns: Txns)(reporter: (RegisterEntry => Unit)): Unit = {

    val registerEngine = new mutable.HashMap[String, BigDecimal]()

    txns.foreach(txn => {
      val registerPostings = txn.posts.map({ p =>
        val newTotal = registerEngine.getOrElse(p.account, BigDecimal(0)) + p.amount
        registerEngine.update(p.account, newTotal)

        RegisterPosting(p, newTotal)
      })

      reporter((txn, registerPostings))
    })
  }
}
