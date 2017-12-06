package fi.sn127.tackler.report

import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.{GroupByMonth, GroupByYear, Settings}

class BalanceGroupSettingsTest extends FlatSpec with MustMatchers {
  val settings = Settings()

  behavior of "BalanceGroupSettings"

  it should "apply with default" in {
    val cfg = BalanceGroupSettings(settings)

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "BALANCE GROUPS"
    cfg.accounts mustBe List[String]()
    cfg.groupBy mustBe a [GroupByMonth]
  }

  it should "apply" in {
    val cfg = BalanceGroupSettings(settings, Some("unit test"), Some(List("a", "b")), Some(GroupByYear()))

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "unit test"
    cfg.accounts mustBe List("a", "b")
    cfg.groupBy mustBe a [GroupByYear]
  }
}
