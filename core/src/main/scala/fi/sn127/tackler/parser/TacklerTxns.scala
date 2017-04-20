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
   * Get Transactions from GIT backend based on configuration
   * read from Settings.
   * Throws an exception in case of error.
   *
   * feature: 06b4a9b1-f48c-4b33-8811-1f32cdc44d7b
   * coverage: "sorted" tested by TODO
   * @return Txns
   */
  @SuppressWarnings(Array(
    "org.wartremover.warts.TraversableOps",
    "org.wartremover.warts.EitherProjectionPartial"))
  def git2Txns(): Txns = {

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
            "Git repository is not found, check config for basedir\n" +
              "Message: [" + e.getMessage + "]"
          throw new TacklerException(msg)
        }
      }
    }

    val tmpResult = getRepo(Paths.get("../tmp/git-perf-data.git")).flatMap(repository => {
      // with managed(new RevWalk(repository))
      // [error]  ambiguous implicit values:
      // [error]    both method reflectiveDisposableResource ...
      // [error]    and method reflectiveCloseableResource ...
      // Let's use makeMangedResource
      val revWalkM = makeManagedResource(new RevWalk(repository))(_.dispose())(List.empty[Class[Throwable]])
      revWalkM.flatMap(revWalk => {
        // a RevWalk allows to walk over commits based on some filtering that is defined

        //val commit = revWalk.parseCommit(lastCommitId)
        //val refStr = "errors/ParseException"
        val refStr = "1e3"

        val refOpt = Option(repository.findRef(refStr))
        val ref = refOpt.getOrElse({
          throw new RuntimeException("Ref Not found")
        })
        val commitId = ref.getObjectId

        //val lastCommitId = repository.resolve(Constants.HEAD)
        //val commitId = repository.resolve("e690c0ce1b4ec64df5dfb5f761c528587effc6c9")

        val commit = revWalk.parseCommit(commitId)
        log.info("git: using commit: " + commit.getName)
        val tree = commit.getTree

        // now try to find files
        managed(new TreeWalk(repository)).map(treeWalk => {
          treeWalk.addTree(tree)
          treeWalk.setRecursive(true)

          treeWalk.setFilter(AndTreeFilter.create(
            PathFilter.create("txns"),
            PathSuffixFilter.create(".txn")))

          // Handle files
          val txns: Iterator[Seq[Transaction]] = for {
            n <- Iterator.continually(treeWalk.next()).takeWhile(p => p === true)
          } yield {

            if (FileMode.REGULAR_FILE.equals(treeWalk.getFileMode(0))) {
              val objectId = treeWalk.getObjectId(0)
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
              log.warn("Found matching object, but it is directory?!?: {}", treeWalk.getPathString)
              Seq.empty[Transaction]
            }
          }
          txns.flatten.toSeq
        }) // treeWalk.close
      }) // revWalk.dispose
    }) // repository.close

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
