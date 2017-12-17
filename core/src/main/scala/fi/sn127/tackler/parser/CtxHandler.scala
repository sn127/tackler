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

import fi.sn127.tackler.core.{AccountException, CommodityException, Settings}
import fi.sn127.tackler.model.{AccountTreeNode, Commodity, Posting, Posts, Transaction, Txns}
import fi.sn127.tackler.parser.TxnParser._

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
  protected def handleAccount(accountCtx: AccountContext, commodity: Option[Commodity]): AccountTreeNode = {

    val account: String = JavaConverters.asScalaIterator(accountCtx.ID().iterator())
      .map(_.getText)
      .mkString(":")

    if (settings.accounts_strict) {
      settings.accounts_coa.find({ case (key, _) => key === account }) match {
        case None =>
          throw new AccountException("Account not found: [" + account + "]")
        case Some((_, value)) =>
          // enhance: check valid set of commodities from settings
          AccountTreeNode(value.account, commodity)
      }
    } else {
      AccountTreeNode(account, commodity)
    }
  }

  protected def handleAmount(amountCtx: AmountContext): BigDecimal = {
    BigDecimal(amountCtx.NUMBER().getText)
  }

  protected def handleClosingPosition(postingCtx: PostingContext): (
    BigDecimal,
    BigDecimal,
    Option[Commodity],
    Option[Commodity]) = {

    val postCommodity = Option(postingCtx.opt_unit()).map(u => {
      Commodity(u.unit().ID().getText)
    })

    val txnCommodity = Option(postingCtx.opt_unit()).flatMap(u => {

      Option(u.opt_position()).fold(Option(Commodity(u.unit().ID().getText))){pos =>
        Option(pos.closing_pos()).map(cp => {
          // Ok, we have closing position, use its commodity
          Commodity(cp.unit().ID().getText)
        })
      }
    })

    val postAmount = handleAmount(postingCtx.amount())

    val txnAmount = Option(postingCtx.opt_unit())
      .fold(postAmount) { u =>
        Option(u.opt_position()).fold(postAmount) { pos =>
          Option(pos.closing_pos()).fold(postAmount)(cp => {
            // Ok, we have closing position, use its value
            postAmount * handleAmount(cp.amount())
          })
        }
      }

    // todo: fix this silliness, see other todo on Posting
    (postAmount, txnAmount, postCommodity, txnCommodity)
  }

  /**
   * Handle one Posting (posting -rule).
   *
   * @param postingCtx posting productions
   * @return Post
   */
  protected def handleRawPosting(postingCtx: PostingContext): Posting = {
    val foo = handleClosingPosition(postingCtx)
    val acctn = handleAccount(postingCtx.account(), foo._3)

    val comment = Option(postingCtx.opt_comment()).map(c => c.comment().text().getText)
    // todo: fix this silliness, see other todo on Posting
    Posting(acctn, foo._1, foo._2, foo._4, comment)
  }

  /**
   * Handle one Transaction (txn -rule).
   *
   * @param txnCtx txn -productions
   * @return transaction
   */
  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  protected def handleTxn(txnCtx: TxnContext): Transaction = {
    val date = handleDate(txnCtx.date())
    val code = Option(txnCtx.code()).map(c => c.code_value().getText.trim)

    val desc = Option(txnCtx.description()).fold[Option[String]](
      None
    )(d => {
      val s = d.text().getText.trim
      if (s.isEmpty) {
        None
      } else {
        Some(s)
      }
    })


    val uuid = Option(txnCtx.txn_meta()).map( meta => {
      val key = meta.txn_meta_key().UUID().getText
      require(key === "uuid") // IE if not

      java.util.UUID.fromString(meta.text().getText.trim)
    })

    // txnCtx.txn_comment is never null, even when there aren't any comments
    // (in that case it will be an empty list)
    val comments = {
      val l = JavaConverters.asScalaIterator(txnCtx.txn_comment().iterator())
        .map(c => c.comment().text().getText).toList
      if (l.isEmpty) {
        None
      } else {
        Some(l)
      }
    }

    val posts: Posts =
      JavaConverters.asScalaIterator(txnCtx.postings().posting().iterator()).map(p => {
        handleRawPosting(p)
      }).toList

    // Check for mixed commodities
    if (posts.map(p => p.txnCommodity.map(c => c.name).getOrElse("")).distinct.size > 1) {
      throw new CommodityException("" +
        "Multiple different commodities are not allowed inside single transaction." +
        uuid.map(u => "\n   txn uuid: " + u.toString).getOrElse(""))
    }

    val last_posting = Option(txnCtx.postings().last_posting()).map(lp => {
      // use same commodity as what other postings are using
      val ate = handleAccount(lp.account(), posts.head.txnCommodity)
      val amount = Posting.txnSum(posts)
      val comment = Option(lp.opt_comment()).map(c => c.comment().text().getText)

      List(Posting(ate, -amount, -amount, posts.head.txnCommodity, comment))
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
