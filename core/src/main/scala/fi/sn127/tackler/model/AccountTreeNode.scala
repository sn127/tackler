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

import fi.sn127.tackler.core.AccountException

/**
 * Account Tree Node (ATN)
 *
 * This must behave correctly with Seq.distinct,
 * so whole thing must have sensible hashcode.
 *
 * @param depth depth of this account
 * @param parent of this account (path)
 * @param account of this posting (path)
 * @param name of this account (leaf)
 * @param commodity commodity for this account tree node
 */
final case class AccountTreeNode(
  depth: Int,
  root: String,
  parent: String,
  account: String,
  name: String,
  commodity: Option[Commodity]) {

  val commStr: String = commodity.map(c => c.name).getOrElse("")

  override def toString: String = {
    account
  }

  def isParentOf(atn: AccountTreeNode): Boolean = {
    this.account === atn.parent && this.commStr === atn.commStr
  }

  def getFull: String = {
    // todo: revisit, in any case prefix must not be valid account name
    commodity.map(c => c.name + "@" + account).getOrElse("@" + account)
  }

  def compareTo(otherTxn: AccountTreeNode): Int = {
    // todo: ATN: more sensible ordering without getFull
    this.getFull.compareTo(otherTxn.getFull)
  }
}


object OrderByATN extends Ordering[AccountTreeNode] {
  def compare(before: AccountTreeNode, after: AccountTreeNode): Int = {
    before.compareTo(after)
  }
}

object AccountTreeNode{

  def groupBy(acc: AccountTreeNode): String = {
    acc.getFull
  }

  @SuppressWarnings(Array(
    "org.wartremover.warts.Overloading",
    "org.wartremover.warts.ListOps"))
  def apply(acc: String, commodity: Option[Commodity]): AccountTreeNode = {

    // todo use main parser to check Chart of Accounts entries
    if (acc.trim.isEmpty) {
      throw new AccountException("Empty account name is not allowed")
    }
    val parts = acc.split(":")

    if (parts.isEmpty) {
      throw new AccountException("Empty account names are not allowed (all sub-components are empty): [" + acc + "]")
    }
    if (parts.map(subpath => subpath.trim.isEmpty).toSeq.exists(_ =!= false)) {
      throw new AccountException("Empty sub-components are not allowed with accounts: [" + acc + "]")
    }

    AccountTreeNode(
      depth = parts.length,
      root = parts.head,
      parent = parts.reverse.drop(1).reverse.mkString(":"),
      account = acc,
      name = parts.last,
      commodity)
  }
}