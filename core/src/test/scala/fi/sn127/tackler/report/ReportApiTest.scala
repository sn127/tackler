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

import io.circe.optics.JsonPath
import org.scalatest.FlatSpec

import fi.sn127.tackler.core.{GroupByIsoWeek, Settings}
import fi.sn127.tackler.parser.TacklerTxns

class ReportApiTest extends FlatSpec {

  val settings = Settings()
  val txnStr =
    """2017-12-14 txn-01
    | e:e14  14
    | a
    |
    |2017-12-15 txn-02
    | x:not·this  15
    | a
    |
    |2017-12-16 txn-03
    | e:e16  16
    | a
    |
    |""".stripMargin

  val tt = new TacklerTxns(settings)
  val txnData = tt.string2Txns(txnStr)


  val _title = JsonPath.root.title.string


  behavior of "Balance report"
  val _accountTreeSum = JsonPath.root.balance.balanceRows.index(0).accountTreeSum.string
  val _delta = JsonPath.root.balance.deltas.index(0).delta.string

  /**
   *  test: f003a816-3107-4398-902f-656479cf1ee5
   */
  it must "work with default settings" in {
    val balSettings = BalanceSettings(settings)
    val rpt = new BalanceReport(balSettings)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("BALANCE"))
    assert(_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_delta.getOption(report) === Some("0.00"))
  }

  /**
   * test: a50327c0-bd16-42a8-82e4-0846be4b5c6f
   */
  it must "accept arguments" in {
    val balCfg = BalanceSettings(settings, Some("Test-Balance"), Some(List("^e.*", "^a.*")))
    val rpt = new BalanceReport(balCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-Balance"))
    assert(_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_delta.getOption(report) === Some("-15.00"))
  }


  behavior of "BalanceGroup report"
  val _balgrp_title = JsonPath.root.balanceGroups.index(0).title.string
  val _balgrp_accountTreeSum = JsonPath.root.balanceGroups.index(0).balanceRows.index(0).accountTreeSum.string
  val _balgrp_delta = JsonPath.root.balanceGroups.index(0).deltas.index(0).delta.string

  /**
   * test: d6fe5451-2d5d-4ced-848a-934fbc5e43ab
   */
  it must "work with default settings" in {
    val balGrpCfg = BalanceGroupSettings(settings)
    val rpt = new BalanceGroupReport(balGrpCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("BALANCE GROUPS"))
    assert(_balgrp_title.getOption(report) === Some("2017-12Z"))
    assert(_balgrp_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_balgrp_delta.getOption(report) === Some("0.00"))
  }

  /**
   * test: f1f8a1ac-452b-47df-b15b-9e9bf176028a
   */
  it must "accept arguments" in {
    val balGrpCfg = BalanceGroupSettings(settings, Some("Test-BalGrp"), Some(List("^e.*", "^a.*")), Some(GroupByIsoWeek()))
    val rpt = new BalanceGroupReport(balGrpCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-BalGrp"))
    assert(_balgrp_title.getOption(report) === Some("2017-W50Z"))
    assert(_balgrp_accountTreeSum.getOption(report) === Some("-45.00"))
    assert(_balgrp_delta.getOption(report) === Some("-15.00"))
  }


  behavior of "Register report"
  val _reg_txn_idx1_desc = JsonPath.root.registerRows.index(1).txn.description.string

  /**
   * test: 12f73e1a-b96c-43da-8031-30765943bc4f
   */
  it must "work with default settings" in {
    val regCfg = RegisterSettings(settings)
    val rpt = new RegisterReport(regCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("REGISTER"))
    assert(_reg_txn_idx1_desc.getOption(report) === Some("txn-02")) // no filter
  }

  /**
   * test: 71b2e53b-57bc-4f6a-8ad8-4864fd370884
   */
  it must "accepts arguments" in {
    val regCfg = RegisterSettings(settings, Some("Test-Register"), Some(List("^e.*")))
    val rpt = new RegisterReport(regCfg)

    val report = rpt.jsonReport(txnData)

    assert(_title.getOption(report) === Some("Test-Register"))
    assert(_reg_txn_idx1_desc.getOption(report) === Some("txn-03")) // with filter
  }
}
