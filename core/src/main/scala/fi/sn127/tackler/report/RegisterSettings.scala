package fi.sn127.tackler.report

import fi.sn127.tackler.core.Settings

class RegisterSettings(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]])
  extends ReportSettings(settings) {

  val title: String = myTitle match {
    case Some(t) => t
    case None => settings.Reports.Register.title
  }

  val accounts: List[String] = myAccounts match {
    case Some(accs) => accs
    case None => settings.Reports.Register.accounts
  }
}

object RegisterSettings {
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings): RegisterSettings = {
    new RegisterSettings(settings, None, None)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def apply(settings: Settings, myTitle: Option[String], myAccounts: Option[List[String]]): RegisterSettings = {
    new RegisterSettings(settings, myTitle, myAccounts)
  }
}