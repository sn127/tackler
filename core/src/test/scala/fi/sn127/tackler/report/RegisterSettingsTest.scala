package fi.sn127.tackler.report

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.Settings

class RegisterSettingsTest extends FlatSpec with MustMatchers {
  val settings = new Settings(Paths.get("not-found-use-defaults"), ConfigFactory.empty())

  behavior of "RegisterSettings"

  it should "apply with default" in {
    val cfg = RegisterSettings(settings)

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "REGISTER"
    cfg.accounts mustBe List[String]()
  }

  it should "apply" in {
    val cfg = RegisterSettings(settings, Some("unit test"), Some(List("a", "b")))

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "unit test"
    cfg.accounts mustBe List("a", "b")
  }
}
