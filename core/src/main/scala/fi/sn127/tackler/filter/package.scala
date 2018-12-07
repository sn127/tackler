/*
 * Copyright 2018 SN127.fi
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
package fi.sn127.tackler

import fi.sn127.tackler.api._
import fi.sn127.tackler.model.Transaction
import cats.implicits._

package object filter {

  implicit object TxnFilterF extends CanTxnFilter[TxnFilter] {

    def filter(tfABC: TxnFilter, txn: Transaction): Boolean = {

      tfABC match {
        case tf: TxnFilterNone => TxnFilterNoneF.filter(tf, txn)
        case tf: TxnFilterAll => TxnFilterAllF.filter(tf, txn)

        // Logicals
        case tf: TxnFilterAND => TxnFilterANDF.filter(tf, txn)
        case tf: TxnFilterOR => TxnFilterORF.filter(tf, txn)
        case tf: TxnFilterNOT => TxnFilterNOTF.filter(tf, txn)

        // TXN Header
        case tf: TxnFilterTxnTSBegin => TxnFilterTxnTSBeginF.filter(tf, txn)
        case tf: TxnFilterTxnTSEnd => TxnFilterTxnTSEndF.filter(tf, txn)
        case tf: TxnFilterTxnCode => TxnFilterTxnCodeF.filter(tf, txn)
        case tf: TxnFilterTxnDescription => TxnFilterTxnDescriptionF.filter(tf, txn)
        case tf: TxnFilterTxnUUID => TxnFilterTxnUUIDF.filter(tf, txn)
        case tf: TxnFilterTxnComments => TxnFilterTxnCommentsF.filter(tf, txn)

        // TXN Postings
        case tf: TxnFilterPostingAccount => TxnFilterPostingAccountF.filter(tf, txn)
        case tf: TxnFilterPostingComment => TxnFilterPostingCommentF.filter(tf, txn)
        case tf: TxnFilterPostingAmountEqual => TxnFilterPostingAmountEqualF.filter(tf, txn)
        case tf: TxnFilterPostingAmountLess => TxnFilterPostingAmountLessF.filter(tf, txn)
        case tf: TxnFilterPostingAmountGreater => TxnFilterPostingAmountGreaterF.filter(tf, txn)
        case tf: TxnFilterPostingCommodity => TxnFilterPostingCommodityF.filter(tf, txn)
      }
    }
  }

  implicit class FilterUtil[A](tf: A) {
    def filter(txn: Transaction)(implicit makesFilter: CanTxnFilter[A]): Boolean = {
      makesFilter.filter(tf, txn)
    }
  }

  /*
   * Test utilities
   */
  implicit object TxnFilterNoneF extends CanTxnFilter[TxnFilterNone] {
    override def filter(tf: TxnFilterNone, txn: Transaction): Boolean = false
  }

  implicit object TxnFilterAllF extends CanTxnFilter[TxnFilterAll] {
    override def filter(tf: TxnFilterAll, txn: Transaction): Boolean = true
  }


  implicit object TxnFilterRootF extends CanTxnFilter[TxnFilterRoot] {

    override def filter(tf: TxnFilterRoot, txn: Transaction): Boolean = {
      tf.txnFilter.filter(txn)
    }
  }

  implicit object TxnFilterANDF extends CanTxnFilter[TxnFilterAND] {

    override def filter(tf: TxnFilterAND, txn: Transaction): Boolean = {
      tf.txnFilters.forall(f => f.filter(txn))
    }
  }

  implicit object TxnFilterORF extends CanTxnFilter[TxnFilterOR] {

    override def filter(tf: TxnFilterOR, txn: Transaction): Boolean = {
      tf.txnFilters.exists(f => f.filter(txn) === true)
    }
  }

  implicit object TxnFilterNOTF extends CanTxnFilter[TxnFilterNOT] {

    override def filter(tf: TxnFilterNOT, txn: Transaction): Boolean = {
      tf.txnFilter.filter(txn) === false
    }
  }

  implicit object TxnFilterTxnTSBeginF extends CanTxnFilter[TxnFilterTxnTSBegin] {

    override def filter(tf: TxnFilterTxnTSBegin, txn: Transaction): Boolean = {
      // why to toInstant? see test: 960cb7e7-b180-4276-a43b-714e53e1789b
      // offsets which cancel each others won't compare correctly otherwise
      tf.begin.toInstant.compareTo(txn.header.timestamp.toInstant) <= 0
    }
  }

  implicit object TxnFilterTxnTSEndF extends CanTxnFilter[TxnFilterTxnTSEnd] {

    override def filter(tf: TxnFilterTxnTSEnd, txn: Transaction): Boolean = {
      txn.header.timestamp.toInstant.isBefore(tf.end.toInstant)
    }
  }

  implicit object TxnFilterTxnCodeF extends CanTxnFilter[TxnFilterTxnCode] {

    override def filter(tf: TxnFilterTxnCode, txn: Transaction): Boolean = {
      tf.rgx.matcher(txn.header.code.getOrElse("")).matches
    }
  }

  implicit object TxnFilterTxnDescriptionF extends CanTxnFilter[TxnFilterTxnDescription] {

    override def filter(tf: TxnFilterTxnDescription, txn: Transaction): Boolean = {
      tf.rgx.matcher(txn.header.description.getOrElse("")).matches
    }
  }

  implicit object TxnFilterTxnUUIDF extends CanTxnFilter[TxnFilterTxnUUID] {

    override def filter(tf: TxnFilterTxnUUID, txn: Transaction): Boolean = {
      txn.header.uuid.exists(_ === tf.uuid)
    }
  }

  implicit object TxnFilterTxnCommentsF extends CanTxnFilter[TxnFilterTxnComments] {

    override def filter(tf: TxnFilterTxnComments, txn: Transaction): Boolean = {
      txn.header.comments.map(cmts => {
        cmts.exists(c => tf.rgx.matcher(c).matches())
      }) match {
        case Some(b) => b
        case None => false
      }
    }
  }


  /*
   *
   * TXN: POSTINGS
   *
   */

  implicit object TxnFilterPostingAccountF extends CanTxnFilter[TxnFilterPostingAccount] {

    override def filter(tf: TxnFilterPostingAccount, txn: Transaction): Boolean = {
      txn.posts.exists(p => tf.rgx.matcher(p.acctn.account).matches())
    }
  }

  implicit object TxnFilterPostingCommentF extends CanTxnFilter[TxnFilterPostingComment] {

    override def filter(tf: TxnFilterPostingComment, txn: Transaction): Boolean = {
      txn.posts.exists(p => {
        p.comment.exists(tf.rgx.matcher(_).matches())
      })
    }
  }

  implicit object TxnFilterPostingAmountEqualF extends CanTxnFilter[TxnFilterPostingAmountEqual] {

    override def filter(tf: TxnFilterPostingAmountEqual, txn: Transaction): Boolean = {
      txn.posts.exists(p => {
        tf.rgx.matcher(p.acctn.account).matches() &&
          p.amount.compare(tf.amount) === 0
      })
    }
  }

  implicit object TxnFilterPostingAmountLessF extends CanTxnFilter[TxnFilterPostingAmountLess] {

    override def filter(tf: TxnFilterPostingAmountLess, txn: Transaction): Boolean = {
      txn.posts.exists(p => {
        tf.rgx.matcher(p.acctn.account).matches() &&
          p.amount.compare(tf.amount) < 0
      })
    }
  }

  implicit object TxnFilterPostingAmountGreaterF extends CanTxnFilter[TxnFilterPostingAmountGreater] {

    override def filter(tf: TxnFilterPostingAmountGreater, txn: Transaction): Boolean = {
      txn.posts.exists(p => {
        tf.rgx.matcher(p.acctn.account).matches() &&
          p.amount.compare(tf.amount) > 0
      })
    }
  }

  implicit object TxnFilterPostingCommodityF extends CanTxnFilter[TxnFilterPostingCommodity] {

    override def filter(tf: TxnFilterPostingCommodity, txn: Transaction): Boolean = {
      txn.posts.exists(p => {
        p.acctn.commodity.exists(c => tf.rgx.matcher(c.name).matches())
      })
    }
  }

}
