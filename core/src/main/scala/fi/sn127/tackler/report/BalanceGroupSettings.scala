package fi.sn127.tackler.report

import fi.sn127.tackler.core.{GroupBy, Settings}

class BalanceGroupSettings(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]], myGroupBy: Option[GroupBy])
  extends ReportSettings(settings) {

  val title: String = myTitle match {
    case Some(t) => t
    case None => settings.Reports.BalanceGroup.title
  }

  val accounts: List[String] = myAccounts match {
    case Some(accs) => accs
    case None => settings.Reports.BalanceGroup.accounts
  }

  val groupBy: GroupBy = myGroupBy match {
    case Some(gb) => gb
    case None => settings.Reports.BalanceGroup.groupBy
  }
}

object BalanceGroupSettings {
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings): BalanceGroupSettings = {
    new BalanceGroupSettings(settings, None, None, None)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]], myGroupBy: Option[GroupBy]): BalanceGroupSettings = {
    new BalanceGroupSettings(settings, myTitle, myAccounts, myGroupBy)
  }
}