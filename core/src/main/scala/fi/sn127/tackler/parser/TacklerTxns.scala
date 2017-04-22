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
import java.nio.file.Path

import better.files.File
import cats.implicits._
import org.eclipse.jgit.lib.{FileMode, Repository}
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.{AndTreeFilter, PathFilter, PathSuffixFilter}
import org.slf4j.{Logger, LoggerFactory}
import resource.{makeManagedResource, managed, _}

import fi.sn127.tackler.core.{Settings, TacklerException}
import fi.sn127.tackler.model.{OrderByTxn, Transaction, Txns}

/**
 * Generate Transactions from selected inputs.
 *
 * These take an input(s) as argument and
 * returns sequence of transactions.
 *
 * If there is an error, they throw an exception.
 *
 * @param settings to control how inputs and txns are handled
 */
class TacklerTxns(val settings: Settings) extends CtxHandler {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Get Transactions from list of input paths.
   * Throws an exception in case of error.
   *
   * @param inputs input as seq of files
   * @return Txns
   */
  def paths2Txns(inputs: Seq[Path]): Txns = {

    inputs.par.flatMap(inputPath => {
      log.debug("txn: {}", inputPath.toString())
      val txnsCtx = TacklerParser.txnsFile(inputPath)
      handleTxns(txnsCtx)
    }).seq.sorted(OrderByTxn)
  }

  /**
   * Get Transactions from GIT based storage.
   * Basic git repository information is read from settings,
   * but input setting (ref or commit id) is an argument.
   * Throws an exception in case of error.
   *
   * feature: 06b4a9b1-f48c-4b33-8811-1f32cdc44d7b
   * coverage: "sorted" tested by 1d2c22c1-e3fa-4cd4-a526-45318c15d13e
   *
   * @param inputRef Left(ref) or Right(commitId)
   * @return Txns
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.TraversableOps",
    "org.wartremover.warts.EitherProjectionPartial"))
  def git2Txns(inputRef: Either[String, String]): Txns = {

    /**
     * Get Git repository as managed resource.
     * Repository must be bare.
     *
     * @param gitdir path/to/repo.git
     * @return repository as managed resource
     */
    def getRepo(gitdir: File): ManagedResource[Repository] = {
      try {
        val repo = (new FileRepositoryBuilder)
          .setGitDir(gitdir.toJava)
          .setMustExist(true)
          .setBare()
          .build()
        managed(repo)
      } catch {
        case e: org.eclipse.jgit.errors.RepositoryNotFoundException => {
          val msg =
            "Git repository is not found, check repository config\n" +
              "Message: [" + e.getMessage + "]"
          log.error(msg)
          throw new TacklerException(msg)
        }
      }
    }

    val tmpResult = getRepo(settings.input_git_repository).flatMap(repository => {

      val commitId = if (inputRef.isLeft) {
        val refOpt = Option(repository.findRef(inputRef.left.get))
        val ref = refOpt.getOrElse({
          throw new TacklerException("Git ref not found or it is invalid: [" + inputRef.left.get + "]")
        })
        ref.getObjectId
      } else {
        val cidOpt = try {
          Option(repository.resolve(inputRef.right.get))
        } catch {
          case e: RuntimeException =>
            val msg =
              "Can not resolve commit by given id: [" + inputRef.right.get + "]\n"
                "Message: [" + e.getMessage + "]"
            log.error(msg)
            throw new TacklerException(msg)
        }

        cidOpt.getOrElse({
          val msg =
          "Can not find commit by given id: [" + inputRef.right.get + "]"
          log.error(msg)
          throw new TacklerException(msg)
        })
      }

      // with managed(new RevWalk(repository))
      // [error]  ambiguous implicit values:
      // [error]    both method reflectiveDisposableResource ...
      // [error]    and method reflectiveCloseableResource ...
      // Let's use makeMangedResource
      val revWalkM = makeManagedResource(new RevWalk(repository))(_.dispose())(List.empty[Class[Throwable]])
      revWalkM.flatMap(revWalk => {
        // a RevWalk allows to walk over commits based on some filtering that is defined

        val commit = try {
          revWalk.parseCommit(commitId)
        } catch {
          case e: org.eclipse.jgit.errors.MissingObjectException =>
            val msg = "Can not find commit by given id\n" +
                "Message: [" + e.getMessage + "]"
            log.error(msg)
            throw new TacklerException(msg)
        }

        log.info("git: using commit: " + commit.getName)
        val tree = commit.getTree

        // now try to find files
        managed(new TreeWalk(repository)).map(treeWalk => {
          treeWalk.addTree(tree)
          treeWalk.setRecursive(true)

          treeWalk.setFilter(AndTreeFilter.create(
            PathFilter.create(settings.input_git_dir),
            PathSuffixFilter.create(settings.input_git_suffix)))

          // Handle files
          val txns: Iterator[Seq[Transaction]] = for {
            n <- Iterator.continually(treeWalk.next()).takeWhile(p => p === true)
          } yield {

            val objectId = treeWalk.getObjectId(0)
            if (FileMode.REGULAR_FILE.equals(treeWalk.getFileMode(0))) {
              val loader = repository.open(objectId)
              log.debug("txn: git: object id: " + objectId.getName + ", path: " + treeWalk.getPathString)

              val txnsResult = managed(loader.openStream).map(stream => {
                handleTxns(TacklerParser.txnsStream(stream))
              })
              if (txnsResult.either.isLeft) {
                // todo: handle error, parse error msg, commit id, etc.
                log.error("Error git: object id: " + objectId.getName + ", path: " + treeWalk.getPathString)
                throw txnsResult.either.left.get.head
              } else {
                txnsResult.either.right.get
              }
            } else {
              val msg = "Found matching object, but it is not regular file\n" +
                "   commit id: " + commit.getName + "\n" +
                "   object id: " + objectId.getName + "\n" +
                "   path: [" + treeWalk.getPathString + "]"
              log.error(msg)
              throw new TacklerException(msg)
            }
          }
          txns.flatten.toSeq
        })
      })
    })
    // https://github.com/jsuereth/scala-arm/issues/49
    val result = tmpResult.map(u => u).either

    val txns = if (result.isRight) {
      result.right.get
    } else {
      throw result.left.get.head
    }

    txns.sorted(OrderByTxn)
  }

  /**
   * Parse and converts input string to Txns
   * Throws an exception in case of error.
   *
   * feature: a94d4a60-40dc-4ec0-97a3-eeb69399f01b
   * coverage: "sorted" tested by 200aad57-9275-4d16-bdad-2f1c484bcf17
   *
   * @param input as text
   * @return Txns
   */
  def string2Txns(input: String): Txns = {

    val txnsCtx = TacklerParser.txnsText(input)
    handleTxns(txnsCtx).sorted(OrderByTxn)
  }
}
