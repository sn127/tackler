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
