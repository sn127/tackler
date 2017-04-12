package fi.sn127.tackler.model

import cats.implicits._

import fi.sn127.tackler.core.AccountException

/**
 * Account tree node
 *
 * @param depth depth of this account
 * @param parent of this account (path)
 * @param account of this posting (path)
 * @param name of this account (leaf)
 */
final case class AccountTreeNode(
  depth: Int,
  root: String,
  parent: String,
  account: String,
  name: String) {

  override def toString: String = {
    account
  }
}

object AccountTreeNode{

  @SuppressWarnings(Array(
    "org.wartremover.warts.Overloading",
    "org.wartremover.warts.ListOps"))
  def apply(acc: String): AccountTreeNode = {

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
      name = parts.last)
  }
}