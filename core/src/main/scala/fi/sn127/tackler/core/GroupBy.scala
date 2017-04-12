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

sealed trait GroupBy
sealed case class GroupByYear() extends GroupBy
sealed case class GroupByMonth() extends GroupBy
sealed case class GroupByDate() extends GroupBy
sealed case class GroupByIsoWeek() extends GroupBy
sealed case class GroupByIsoWeekDate() extends GroupBy

object GroupBy {
  def apply(groupBy: String): GroupBy = {
    groupBy match {
      case "year" => GroupByYear()
      case "month" => GroupByMonth()
      case "date" => GroupByDate()
      case "iso-week" => GroupByIsoWeek()
      case "iso-week-date" => GroupByIsoWeekDate()
      /* Error*/
      case _ => throw new GroupByException(
        "Unknown group-by operator. Valid operators are: year, month, date, iso-week, iso-week-date")
    }
  }
}
