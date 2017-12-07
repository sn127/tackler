/*
 * Copyright 2017 Jani Averbach
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