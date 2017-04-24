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
package fi.sn127.tackler.parser

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import cats.implicits._

import scala.collection.JavaConverters

import fi.sn127.tackler.core.{AccountException, Settings}
import fi.sn127.tackler.model.{AccountTreeNode, Posting, Posts, Transaction, Txns}
import fi.sn127.tackler.parser.TxnParser.{AccountContext, DateContext, PostingContext, TxnContext, TxnsContext}

/**
 * Handler utilities for ANTLR Parser Contexts.
 *
 * This handlers convert Parser Contexts to
 * Tackler Model (to Transactions, Postings, etc).
 */
abstract class CtxHandler {
  val settings: Settings

  /**
   * Handle raw parser date productions,
   * and convert to ZonedDateTime (date -rule).
   *
   * @param dateCtx date productions
   * @return zoned ts
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.OptionPartial"))
  protected def handleDate(dateCtx: DateContext): ZonedDateTime = {

    val tzDate: ZonedDateTime =
      Option(dateCtx.TS_TZ()) match {
        case Some(tzTS) => {
          ZonedDateTime.parse(tzTS.getText,
            java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
        case None => {
          Option(dateCtx.TS()) match {
            case Some(localTS) => {
              val dt = LocalDateTime.parse(localTS.getText,
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              ZonedDateTime.of(dt, settings.timezone)
            }
            case None => {
              val optDate = Option(dateCtx.DATE())
              require(optDate.isDefined) // IE if not

              val d = LocalDate.parse(optDate.get.getText,
                java.time.format.DateTimeFormatter.ISO_DATE)

              ZonedDateTime.of(d, settings.defaultTime, settings.timezone)
            }
          }
        }
      }
    tzDate
  }

  /**
   * Handle raw parser account entry (account -rule).
   *
   * @param accountCtx account context
   * @return Account tree node
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.TraversableOps",
    "org.wartremover.warts.ListOps"))
  protected def handleAccount(accountCtx: AccountContext): AccountTreeNode = {

    val account: String = JavaConverters.asScalaIterator(accountCtx.ID().iterator())
      .map(_.getText)
      .mkString(":")

    if (settings.accounts_strict) {
      settings.accounts_coa.find({ case (key, _) => key === account }) match {
        case None =>
          throw new AccountException("Account not found: [" + account + "]")
        case Some((_, value)) =>
          value
      }
    } else {
      AccountTreeNode(account)
    }
  }

  /**
   * Handle on [[Posting]] (posting -rule).
   *
   * @param postingCtx posting productions
   * @return Post
   */
  protected def handleRawPosting(postingCtx: PostingContext): Posting = {
    val acctn = handleAccount(postingCtx.account())
    val amount = BigDecimal(postingCtx.amount().NUMBER().getText)
    val comment = Option(postingCtx.comment()).map(c => c.text().getText)

    Posting(acctn, amount, comment)
  }

  /**
   * Handle one [[Transaction]] (txn -rule).
   *
   * @param txnCtx txn -productions
   * @return transaction
   */
  protected def handleTxn(txnCtx: TxnContext): Transaction = {
    val date = handleDate(txnCtx.date())
    val code = Option(txnCtx.code()).map(c => c.code_value().getText.trim)
    val desc = Option(txnCtx.description()).map(d => d.text().getText.trim)

    val uuid = Option(txnCtx.txn_meta()).map( meta => {
      val key = meta.txn_meta_key().UUID().getText
      require(key === "uuid") // IE if not

      java.util.UUID.fromString(meta.text().getText.trim)
    })

    val comments = Option(txnCtx.txn_comment()).map(cs =>
      JavaConverters.asScalaIterator(cs.iterator())
        .map(c => c.comment().text().getText).toList
    )

    val posts: Posts =
      JavaConverters.asScalaIterator(txnCtx.postings().posting().iterator()).map(p => {
        handleRawPosting(p)
      }).toList

    val last_posting = Option(txnCtx.postings().last_posting()).map(lp => {
      val ate = handleAccount(lp.account())
      val amount = Posting.sum(posts)
      val comment = Option(lp.comment()).map(c => c.text().getText)
      List(Posting(ate, -amount, comment))
    })

    Transaction(date, code, desc, uuid, comments, posts ++ last_posting.getOrElse(Nil))
  }

  /**
   * Handle multiple transaction productions (txns -rule).
   *
   * @param txnsCtx txns productions
   * @return sequence of Transactions.
   */
  protected def handleTxns(txnsCtx: TxnsContext): Txns = {
    JavaConverters.asScalaIterator(txnsCtx.txn().iterator())
      .map({ case (rawTxn) =>
        handleTxn(rawTxn)
      }).toSeq
  }
}
