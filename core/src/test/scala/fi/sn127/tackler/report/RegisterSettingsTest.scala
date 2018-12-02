/*
 * Copyright 2017 SN127.fi
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

import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.Settings

class RegisterSettingsTest extends FlatSpec with MustMatchers {
  val settings = Settings()

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
