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

import java.util.regex.Pattern

import cats.implicits._

import fi.sn127.tackler.model.{BalanceTreeNode, RegisterPosting}


/**
 * Select all Accounts on Balance Report.
 */
object AllBalanceAccounts extends Filtering[BalanceTreeNode] {
  override def predicate(x: BalanceTreeNode): Boolean = true
}

/**
 * Filter Accounts on Balance Report based on account name,
 * e.g. select all accounts which match some of given patterns.
 *
 * @param patterns list of account name regexs
 */
class BalanceFilterByAccount(val patterns: Seq[String]) extends Filtering[BalanceTreeNode]{
  private val regexs = patterns.map(name => {Pattern.compile(name)})

  override def predicate(x: BalanceTreeNode): Boolean = {
    regexs.exists(_.matcher(x.acctn.account).matches())
  }
}

/**
 * Filter Accounts on Balance Report based on amount
 * e.g. select all accounts which have non-zero posting amount
 */
class BalanceFilterNonZero() extends Filtering[BalanceTreeNode]{
  override def predicate(x: BalanceTreeNode): Boolean = {
    x.accountSum =!= 0
  }
}

/**
 * Filter accounts based on name, and Non-Zero status
 * e.g. select all accounts which match regex, and have non-zero posting amount
 *
 * @param patterns list of account name regexs
 */
class BalanceFilterNonZeroByAccount(patterns: Seq[String]) extends BalanceFilterByAccount(patterns) {
  override def predicate(x: BalanceTreeNode): Boolean = {
    super.predicate(x) && x.accountSum =!= 0
  }
}

/**
 * Select all RegisterPostings (e.g. Account rows).
 */
object AllRegisterPostings extends Filtering[RegisterPosting] {
  override def predicate(x: RegisterPosting): Boolean = true
}

/**
 * Filter RegisterPostings based on account name,
 * e.g. select all RegisterPostings which match some
 * of given patterns.
 *
 * @param patterns list of account name regexs
 */
final case class RegisterFilterByAccount(patterns: Seq[String]) extends Filtering[RegisterPosting]{
  private val regexs= patterns.map(name => {Pattern.compile(name)})

  override def predicate(x: RegisterPosting): Boolean = {
    regexs.exists(_.matcher(x.post.acctn.account).matches())
  }
}
