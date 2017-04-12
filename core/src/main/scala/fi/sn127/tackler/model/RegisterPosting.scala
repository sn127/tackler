package fi.sn127.tackler.model

object OrderByRegPosting extends Ordering[RegisterPosting] {
  def compare(before: RegisterPosting, after: RegisterPosting): Int = {
    before.post.acctn.account.compareTo(after.post.acctn.account)
  }
}

final case class RegisterPosting(
  post: Posting,
  runningTotal: BigDecimal
) {
  def account: String = post.account
  def amount: BigDecimal = post.amount
}
