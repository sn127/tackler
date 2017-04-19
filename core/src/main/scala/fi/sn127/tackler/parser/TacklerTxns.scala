/*
 * Copyright 2016-2017 Jani Averbach
 *
 * git2Txns is based on example
 * by: Copyright 2013, 2014 Dominik Stadler
 * license: Apache License v2.0
 * url: https://raw.githubusercontent.com/centic9/jgit-cookbook/
 * commit: 276ad0fecb4f1c616ef459ed8b7feb6d503724eb
 * file: jgit-cookbook/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java
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
import java.nio.file.{Path, Paths}
import java.time._

import better.files.File
import cats.implicits._
import org.eclipse.jgit.lib.{FileMode, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{AndTreeFilter, PathFilter, PathSuffixFilter}
import org.slf4j.{Logger, LoggerFactory}
import resource._

import scala.collection.JavaConverters

import fi.sn127.tackler.core.{AccountException, Settings, TacklerException}
import fi.sn127.tackler.model.{AccountTreeNode, OrderByTxn, Posting, Posts, Transaction, Txns}
import fi.sn127.tackler.parser.TxnParser._

class TacklerTxns(val settings: Settings) {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Parse and convert list of input paths to Txns
   *
   * @param inputs input as seq of files
   * @return Txns
   */
  def inputs2Txns(inputs: Seq[Path]): Txns = {

    inputs.par.flatMap(inputPath => {
      log.debug("txn: {}", inputPath.toString())
      val txnsCtx = TacklerParser.txnsFile(inputPath)
      handleTxns(txnsCtx)
    }).seq.sorted(OrderByTxn)
  }

  @SuppressWarnings(Array("org.wartremover.warts.While"))
  def git2Txns(): Txns = {
    def getRepo(gitdir: File): Repository = {
      try {
        (new FileRepositoryBuilder)
          .setGitDir(gitdir.toJava)
          .setMustExist(true)
          .setBare()
          .build()
      } catch {
        case e: org.eclipse.jgit.errors.RepositoryNotFoundException => {
          val msg =
            "Git repository is not found, check config for basedir\n" +
              "Message: [" + e.getMessage + "]"
          throw new TacklerException(msg)
        }
      }
    }

    val repository = getRepo(Paths.get("../tmp/test.git"))

    // a RevWalk allows to walk over commits based on some filtering that is defined
    val revWalk = new RevWalk(repository)

    //
    // find the HEAD
    //

    //val lastCommitId = repository.resolve(Constants.HEAD)
    //val commit = revWalk.parseCommit(lastCommitId)
    val head = repository.findRef("mini") // todo: null if not found
    val commitId = head.getObjectId

    val commit = revWalk.parseCommit(commitId)
    val tree = commit.getTree

    // now try to find files
    val foo: Seq[Transaction] = managed(new TreeWalk(repository))
      .map(treeWalk => {
        treeWalk.addTree(tree)
        treeWalk.setRecursive(true)

        treeWalk.setFilter(AndTreeFilter.create(
          PathFilter.create("txns-mini"),
          PathSuffixFilter.create(".txn")))

        // Handle files
        val txns: Iterator[Seq[Transaction]] = for {
          n <- Iterator.continually(treeWalk.next()).takeWhile(p => p === true)
        } yield {

          if (FileMode.REGULAR_FILE.equals(treeWalk.getFileMode(0))) {
            log.debug("txn: {}", treeWalk.getPathString)

            val objectId = treeWalk.getObjectId(0)
            val loader = repository.open(objectId)

            managed(loader.openStream).map(stream => {
              handleTxns(TacklerParser.txnsStream(stream))
            }).opt match {
              case Some(t) => t
              case None => Seq.empty[Transaction]
            }
          } else {
            log.warn("Found matching object, but it is directory?!?: {}", treeWalk.getPathString)
            Seq.empty[Transaction]
          }
        }
        //treeWalk.close()
        txns.flatten.toSeq
      }).opt match {
      case Some(t) => t
      case None => Seq.empty[Transaction]
    }

    revWalk.dispose()
    repository.close()
    foo.sorted(OrderByTxn)
  }

  /**
   * Parse and converts input string to Txns
   *
   * feature: a94d4a60-40dc-4ec0-97a3-eeb69399f01b
   * coverage: "Sorted" tested by 200aad57-9275-4d16-bdad-2f1c484bcf17
   *
   * @param input as text
   * @return Txns
   */
  def input2Txns(input: String): Txns = {

    val txnsCtx = TacklerParser.txnsText(input)
    handleTxns(txnsCtx).sorted(OrderByTxn)
  }

  @SuppressWarnings(Array(
    "org.wartremover.warts.OptionPartial", "OptionGet"))
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
   * Handle raw parser account entry.
   *
   * @param accountCtx from parser
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

  protected def handleRawPosting(postingCtx: PostingContext): Posting = {
    val acctn = handleAccount(postingCtx.account())
    val amount = BigDecimal(postingCtx.amount().NUMBER().getText)
    val comment = Option(postingCtx.comment()).map(c => c.text().getText)

    Posting(acctn, amount, comment)
  }

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

  protected def handleTxns(txnsCtx: TxnsContext): Txns = {
    JavaConverters.asScalaIterator(txnsCtx.txn().iterator())
      .map({ case (rawTxn) =>
        handleTxn(rawTxn)
      }).toSeq
  }
}
