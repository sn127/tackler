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

import cats.implicits._

import fi.sn127.tackler.model.{AccountTreeNode, BalanceTreeNode, OrderByPost, Posting, Txns}


class Balance(val title: String, val bal: Seq[BalanceTreeNode], val delta: BigDecimal) {
  def isEmpty: Boolean = bal.isEmpty
}

object Balance {

  /**
   * Recursive get balance tree nodes, starting from "me"
   *
   * @param me is name of root account for this sub-tree
   * @param accSums list of all account sums
   * @return list of balance tree nodes
   */
   protected def getBalanceTreeNodes(
    me: (AccountTreeNode, BigDecimal),
    accSums: Seq[(AccountTreeNode, BigDecimal)])
  : Seq[BalanceTreeNode] = {

    val (myAccTN, mySum) = me

    // find my childs
    val childs = accSums.filter({ case (atc, _) =>
      atc.parent === myAccTN.account
    })

    // calculate balance tree nodes of my childs
    val childsBalanceTrees = childs.flatMap(c =>
      getBalanceTreeNodes(c, accSums)
    )

    // calculate top sum of my children's balance trees
    // (as it is  needed for my own balance tree node)
    val myChildsSum = childsBalanceTrees
      .filter(btn => btn.acctn.parent === myAccTN.account)
      .map(btn => btn.subAccTreeSum)
      .sum

    val myBTN = BalanceTreeNode(myAccTN, myChildsSum + mySum, mySum)

    List(myBTN) ++ childsBalanceTrees
  }

  /**
   * Bubble up from leafs to root, and generate for any missing (gap)
   * AccountTreeNode new entry with zero sum.
   *
   * @param myAccTNSum starting AccTNSum entry
   * @param accSums current incomplete (in sense of Chart of Account) account sums
   * @return for this branch (from leaf to root) new set of AccTNSums without gaps
   */
  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps", "TraversableHead"))
  protected def bubbleUpAccTN(
    myAccTNSum: (AccountTreeNode, BigDecimal),
    accSums: Seq[(AccountTreeNode, BigDecimal)])
  : Seq[(AccountTreeNode, BigDecimal)] = {

    val myAccTN = myAccTNSum._1

    if (myAccTN.depth === 1) {
      // we are on top, so either "I" exist already
      // or I has been created by my child;
      // end of recursion
      List(myAccTNSum)
    } else {
      // Not on top => find my parent(s)
      val parent = accSums.filter({ case (atc, _) => myAccTN.parent === atc.account })

      assert(parent.isEmpty || parent.length === 1)

      if (parent.isEmpty) {
        if (myAccTN.depth > 2) {

          val par = myAccTN.parent.substring(0, myAccTN.parent.lastIndexOf(":"))
          val account = myAccTN.parent
          val name = myAccTN.parent.split(":").last

          val newParent = AccountTreeNode(myAccTN.depth - 1, myAccTN.root, par, account, name)
          bubbleUpAccTN((newParent, BigDecimal(0)), accSums) ++ List(myAccTNSum)
        } else {
          // I am depth 2 and I don't have parent, => let's create root account
          // end of recursion
          // todo Chart of Accounts...
          val par = ""
          val account = myAccTN.parent
          val name = myAccTN.parent

          val newParent = AccountTreeNode(myAccTN.depth - 1, myAccTN.root, par, account, name)
          List((newParent, BigDecimal(0)), myAccTNSum)
        }
      } else {
        // my parent exists, just bubble up together
        bubbleUpAccTN(parent.head, accSums) ++ List(myAccTNSum)
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps", "TraversableHead"))
  protected def balance(txns: Txns): Seq[BalanceTreeNode] = {

    //    Calculate sum of postings for each account,
    //    resulting size of this set is "small"
    //    e.g. max size is size of Chart of Accounts
    // TODO: AccountTreeNode: provide default groupBy machinery
    val accountSums: Seq[(AccountTreeNode, BigDecimal)] = txns
      .flatMap(txn => txn.posts)
      .groupBy(_.acctn.account)
      .map((kv: (String, Seq[Posting])) => {
        val post = kv._2.head
        val accSum = kv._2.map(_.amount).sum
        (post.acctn, accSum)
      }).toSeq


    //    From every account bubble up and insert missing parent AccTNs.
    //    This will generate duplicates, because we are arriving from different branches
    //    to the same fork in trunk.
    //    (we are using old incomplete set of AccTNSums, not the new, complete set,
    //    which will be the result of this function, so the same fork will be "missing"
    //    multiple times.
    val completeCoASumTree = accountSums.flatMap({ acc =>
      bubbleUpAccTN(acc, accountSums)
    }).distinct

    //    Get all root accounts
    val roots: Seq[(AccountTreeNode, BigDecimal)] =
      completeCoASumTree.filter({case (acctn, _) => acctn.depth === 1})

    //    Start from root's and get all subtree BalanceTreeNodes
    val bal = roots.flatMap(rootAccSum => {
      getBalanceTreeNodes(rootAccSum, completeCoASumTree)
    })

    bal.sorted(OrderByPost)
  }

  def apply(title: String, txns: Txns, accounts: Filtering[BalanceTreeNode]): Balance = {
    val bal = balance(txns)

    val fbal = bal.filter(accounts.predicate)
    if (fbal.nonEmpty) {

      val delta = fbal.map(_.accountSum).sum
      new Balance(title, fbal, delta)
    } else {
      new Balance(title, Seq.empty[BalanceTreeNode], 0.0)
    }
  }
}