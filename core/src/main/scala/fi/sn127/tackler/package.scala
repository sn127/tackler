/*
 * Copyright 2018 sn127.fi
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
package fi.sn127

/**
 * [[https://github.com/sn127/tackler Tackler]] core library
 *
 * = Base components =
 *  - [[fi.sn127.tackler.core]] Core functionality
 *  - [[fi.sn127.tackler.model]] Data models
 *  - [[fi.sn127.tackler.parser]] Parser and utilities
 *  - [[fi.sn127.tackler.report]] Reporting utilites, reports and exports
 *
 * == How to use Tackler programmatically ==
 *
 *  - Acquire settings: [[fi.sn127.tackler.core.Settings$]]
 *  - Create sequence of Txns: [[fi.sn127.tackler.parser.TacklerTxns]]
 *  - Settings for reports, e.g. [[fi.sn127.tackler.report.BalanceSettings]]
 *  - Create actual report by [[fi.sn127.tackler.report.BalanceReporter]]
 *  - Acquire report
 *    - json object: [[fi.sn127.tackler.report.BalanceReporter.jsonReport]]
 *    - io-output: [[fi.sn127.tackler.report.BalanceReporter.writeReport]]
 */
package object tackler {

}
