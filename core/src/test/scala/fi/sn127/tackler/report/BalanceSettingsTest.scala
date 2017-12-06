package fi.sn127.tackler.report

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.Settings

class BalanceSettingsTest extends FlatSpec with MustMatchers {
  val settings = new Settings(Paths.get("not-found-use-defaults"), ConfigFactory.empty())

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
