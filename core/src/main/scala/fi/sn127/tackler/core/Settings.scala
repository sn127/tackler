/*
 * Copyright 2016-2017 Jani Averbach
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

import java.io.{IOException, File => JFile}
import java.nio.file.Path
import java.time.{LocalTime, ZoneId}

import better.files._
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

import fi.sn127.tackler.model.AccountTreeNode

/**
 * Config keys / paths. All of these keys / paths
 * must be available (by embedded default conf).
 */
object CfgKeys {
  val timezone: String = "timezone"

  val basedir: String = "basedir"

  val input_txn_dir: String = "input.txn.dir"
  val input_txn_glob: String = "input.txn.glob"

  val accounts_strict: String = "accounts.strict"
  val accounts_coa: String = "accounts.coa"

  val reporting_reports: String  = "reporting.reports"
  val reporting_accounts: String = "reporting.accounts"
  val reporting_console: String = "reporting.console"

  object Reports {
    protected val keybase: String = "reports"

    object Balance {
      protected val keybase: String = Reports.keybase + "." + "balance"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
    }

    object BalanceGroup {
      protected val keybase: String = Reports.keybase + "." + "balance-group"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
      val groupBy: String = keybase + "." + "group-by"
    }

    object Register {
      protected val keybase: String = Reports.keybase + "." + "register"

      val title: String = keybase + "." + "title"
      val accounts: String = keybase + "." + "accounts"
    }

    object Equity {
      protected val keybase: String = Reports.keybase + "." + "equity"

      // Export: => no title
      val accounts: String = keybase + "." + "accounts"
    }

  }
}

class Settings(cfgPath: Path, cliCfgSettings: Config) {
  /**
   * This is a basename of default Config resource.
   */
  private val basename = "tackler"

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def ensurePath(path: Path): Try[Path] = {
    try {
      Success[Path](path.toRealPath())
    } catch {
      case ex: IOException => Failure[Path](ex)
    }
  }

  val cfg: Config = ensurePath(cfgPath) match {
    case Success(_) =>
      log.debug("loading provided cfg-file: " + cfgPath.toString)

      cliCfgSettings
        .withFallback(ConfigFactory.parseFile(cfgPath.toFile))
        .withFallback(ConfigFactory.load(basename))
        .resolve()

    case Failure(_) =>
      log.info("loading embedded configuration, because cfg-file was not found: " + cfgPath.toString)

      cliCfgSettings
        .withFallback(ConfigFactory.load(basename))
        .resolve()
  }

  /**
   * Default timezone to be used in case of missing ZoneId / Offset
   */
  val timezone: ZoneId = ZoneId.of(cfg.getString(CfgKeys.timezone))

  /**
   * Default time to be used if time component is missing from Txn
   * Far-Far-Away: defaultTime could be set by conf
   */
  val defaultTime: LocalTime = LocalTime.MIN

  val basedir: Path = getPathWithAnchor(cfg.getString(CfgKeys.basedir), cfgPath)

  val input_txn_dir: Path = getPathWithAnchor(cfg.getString(CfgKeys.input_txn_dir), basedir)
  val input_txn_glob: String = cfg.getString(CfgKeys.input_txn_glob)


  /**
   * Reporting
   */
  // todo: rename, reporting -> reports? similar dot notation?
  // Far-Far-Away: scales could be set by conf
  // Far-Far-Away: scales could be per report settings
  val minScale: Int = 2
  val maxScale: Int = 7

  val reports: List[ReportType] = cfg.getStringList(CfgKeys.reporting_reports).asScala
    .map(ReportType(_)).toList

  val accounts: List[String] = cfg.getStringList(CfgKeys.reporting_accounts).asScala.toList

  val console: Boolean = cfg.getBoolean(CfgKeys.reporting_console)

  val accounts_strict: Boolean = cfg.getBoolean(CfgKeys.accounts_strict)

  val accounts_coa: Map[String, AccountTreeNode] = cfg.getStringList(CfgKeys.accounts_coa).asScala
    .toSet[String].map(acc => (acc, AccountTreeNode(acc))).toMap

  object Reports {
    object Balance {
      protected val keys = CfgKeys.Reports.Balance

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
    }

    object BalanceGroup {
      protected val keys = CfgKeys.Reports.BalanceGroup

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
      // todo: this is lazy evaluated?
      // to trigger, remove output from
      // tests/reporting/ex/GroupByException-unknown-group-by.exec
      // test:uuid: 31e0bd80-d4a9-4d93-915d-fa2424aedb84
      val groupBy: GroupBy = GroupBy(cfg.getString(keys.groupBy))
    }

    object Register {
      protected val keys = CfgKeys.Reports.Register

      val title: String = cfg.getString(keys.title)
      val accounts: List[String] = getReportAccounts(keys.accounts)
    }

    object Equity {
      protected val keys = CfgKeys.Reports.Equity

      val accounts: List[String] = getReportAccounts(keys.accounts)
    }
  }

  def getReportAccounts(key: String): List[String] = {
    if (cfg.hasPath(key)) {
      cfg.getStringList(key).asScala.toList
    } else {
      this.accounts
    }
  }

  /**
   * Translates string to path.
   * If param path as string is not absolute, returns
   * absolute path based on basedir.
   *
   * If param path is absolute, then just translates it
   * to canonical Path.
   *
   * @param path as string
   * @return abs Path
   */
  def getPathWithSettings(path: String): Path = {
    getPathWithAnchor(path, basedir)
  }

  /**
   * Get path with anchor (helper for Better-files).
   *
   * @param path relative or absolute
   * @param anchor used as anchor if path is not absolute
   * @return resulting absolute path
   */
  private def getPathWithAnchor(path: String, anchor: Path): Path = {
    File(File(anchor), path).path
  }
}
