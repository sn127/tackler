package fi.sn127.tackler.report

import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.Settings

class BalanceSettingsTest extends FlatSpec with MustMatchers {
  val settings = Settings()

  behavior of "BalanceSettings"

  it should "apply with default" in {
    val cfg = BalanceSettings(settings)

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "BALANCE"
    cfg.accounts mustBe List[String]()
  }

  it should "apply" in {
    val cfg = BalanceSettings(settings, Some("unit test"), Some(List("a", "b")))

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "unit test"
    cfg.accounts mustBe List("a", "b")
  }
}
