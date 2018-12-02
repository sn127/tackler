/*
 * Copyright 2017-2018 SN127.fi
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

import org.scalatest.{FlatSpec, MustMatchers}

class ReportTypeTest extends FlatSpec with MustMatchers {
  behavior of "ReportType"

  it should "accept balance" in {
    ReportType(Settings.balance) mustBe a[BalanceReportType]
  }
  it should "accept balance-group" in {
    ReportType(Settings.balanceGroup) mustBe a[BalanceGroupReportType]
  }
  it should "accept register" in {
    ReportType(Settings.register) mustBe a[RegisterReportType]
  }

  it should "not accepts (equity)" in {
    assertThrows[ReportException]{
      ReportType(Settings.equity)
    }
  }
  it should "not accept export (identity)" in {
    assertThrows[ReportException]{
      ReportType(Settings.identity)
    }
  }
}

class ExportTypeTest extends FlatSpec with MustMatchers {

  behavior of "ExportType"

  it should "accept equity" in {
    ExportType(Settings.equity) mustBe a[EquityExportType]
  }

  it should "accept identity" in {
    ExportType(Settings.identity) mustBe a[IdentityExportType]
  }

  it should "not accept reports (balance)" in {
    assertThrows[ExportException]{
      ExportType(Settings.balance)
    }
  }
  it should "not accept reports (balance-group)" in {
    assertThrows[ExportException]{
      ExportType(Settings.balanceGroup)
    }
  }
  it should "not accept reports (register)" in {
    assertThrows[ExportException]{
      ExportType(Settings.register)
    }
  }
}
