/*
 * Copyright 2016-2018 sn127.fi
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
package fi.sn127.tackler.tools.generator

import java.io.{File => JFile}
import java.time.format.DateTimeFormatter
import java.time.{Duration, ZoneId, ZonedDateTime}

import better.files._

object Generator {
  def run(args: Array[String]): Unit = {

    val cliCfg = new GeneratorCLIArgs(args)

    val countStr = cliCfg.count.getOrElse("1E3")
    val count =   countStr match {
      case "1E3" => 1000
      case "1E4" => 10000
      case "1E5" => 100000
      case "1E6" => 1000000
      case _ => throw new RuntimeException("Unknown count, should be [1E3, 1E4, 1E5, 1E6] it was: " + countStr)
    }


    val basedir = cliCfg.basedir.getOrElse("./data")
    val txnsDir = File(basedir, s"perf-$countStr" )

    val startTS = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.of("Z"))
    val endTS = ZonedDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneId.of("Z"))
    val duration = Duration.between(startTS, endTS)
    val step = duration.getSeconds / count

    if (cliCfg.single_file.getOrElse(false)) {
      File(basedir).createDirectories()
      val txnFile = File(basedir, "perf-" + countStr + ".txn")
      txnFile.createIfNotExists().overwrite("")
    }

    val accounts: Seq[List[String]] = for (i <- 1 to count) yield {
      val ts = startTS.plusSeconds(i * step)
      val y = ts.getYear
      val m = ts.getMonthValue
      val d = ts.getDayOfMonth

      val assetsAcc = "a:ay%04d:am%02d".format(y, m)
      val expensesAcc = "e:ey%04d:em%02d:ed%02d".format(y, m, d)

      val compatStr = if (cliCfg.compatible.getOrElse(false)) {
        (ts.format(DateTimeFormatter.ofPattern("yyyy'/'MM'/'dd")), "  ")
      } else {
        (ts.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), " ")
      }

      compatStr match {
        case (tsStr, valSpace) =>

          val txn = tsStr + s" $i\n" +
            s""" $expensesAcc$valSpace$d.0000001
               | $assetsAcc
               |
           |""".stripMargin

          if (cliCfg.single_file.getOrElse(false)) {
            val txnFile = File(basedir, "perf-" + countStr + ".txn")
            txnFile.append(txn)
          } else {
            val txnName = ts.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")) + s"-$i.txn"
            val txnShardDir = txnsDir / "%04d/%02d/%02d".format(y, m, d)
            val txnFile = txnShardDir / txnName

            txnShardDir.createDirectories()
            txnFile.createIfNotExists().overwrite(txn)
          }

          List(expensesAcc, assetsAcc)
      }
    }

    val coaConf = accounts.flatten.sorted.distinct.mkString("accounts.coa = [\n\"", "\",\n\"", "\"\n]\n")
    val coaFile = File(basedir, s"perf-$countStr-coa.conf")
    coaFile.overwrite(coaConf)
  }

  def main(args: Array[String]): Unit = {
    run(args)
    System.exit(0)
  }
}
