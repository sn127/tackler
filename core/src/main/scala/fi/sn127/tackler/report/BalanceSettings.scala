package fi.sn127.tackler.report

import fi.sn127.tackler.core.Settings

class BalanceSettings(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]])
  extends ReportSettings(settings) {

  val title: String = myTitle match {
    case Some(t) => t
    case None => settings.Reports.Balance.title
  }

  val accounts: List[String] = myAccounts match {
    case Some(accs) => accs
    case None => settings.Reports.Balance.accounts
  }
}

object BalanceSettings {
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings): BalanceSettings = {
    new BalanceSettings(settings, None, None)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]]): BalanceSettings = {
    new BalanceSettings(settings, myTitle, myAccounts)
  }
}