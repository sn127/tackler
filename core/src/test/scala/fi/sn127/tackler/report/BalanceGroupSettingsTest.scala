/*
 * Copyright 2017 sn127.fi
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
