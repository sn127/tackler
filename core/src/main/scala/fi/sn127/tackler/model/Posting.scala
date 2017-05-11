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
import cats.implicits._

import fi.sn127.tackler.core.TxnException

final case class Posting(
  acctn: AccountTreeNode,
  amount: BigDecimal,
  // todo: fix / rename these (position?, exchange? amount, commodity)
  txnAmount: BigDecimal,
  txnCommodity: Option[Commodity],
  comment: Option[String]) {

  if (amount.compareTo(BigDecimal(0)) === 0) {
    throw new TxnException("Zero sum postings are not allowed (is it typo?): " + acctn.account)
  }

  def atnKey: String = acctn.getFull
  // todo: ATN and commodity printing: fix this
  def account: String = acctn.commodity.map(c => c.name + " " + acctn.account).getOrElse(acctn.account)

  override
  def toString: String = {
    val missingSign = if (amount < 0) "" else " "
    acctn.toString + "  " +
      missingSign + amount.toString() + acctn.commodity.map(c => " " + c.name).getOrElse("") +
      txnCommodity.map(txnC => {
        // todo: fix this
        if (txnC.name === acctn.commStr) {
          ""
        } else {
          " @ " + (txnAmount / amount).toString() + " " + txnC.name
        }
      }).getOrElse("") +
      comment.map(c => " ; " + c).getOrElse("")
  }
}

object Posting {

  def txnSum(posts: Posts): BigDecimal = {
    posts.foldLeft(BigDecimal(0))(_ + _.txnAmount)
  }
}
