package fi.sn127.tackler.model

final case class BalanceTreeNode(
  acctn: AccountTreeNode,
  subAccTreeSum: BigDecimal,
  accountSum: BigDecimal
)

object OrderByPost extends Ordering[BalanceTreeNode] {
  def compare(before: BalanceTreeNode, after: BalanceTreeNode): Int = {
    before.acctn.account.compareTo(after.acctn.account)
  }
}
