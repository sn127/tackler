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
package fi.sn127.tackler.parser

import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path

import org.antlr.v4.runtime._
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.slf4j.{Logger, LoggerFactory}

import fi.sn127.tackler.parser.TxnParser.TxnsContext

object TacklerParser {
  val log: Logger = LoggerFactory.getLogger(TacklerParser.getClass)

  def txnsText(inputText: String): TxnsContext = {
    try {
      parseTxns(CharStreams.fromString(inputText))
    } catch {
      case ex: ParseCancellationException =>
        val maxChars = 1024
        val msg = "Txn Parse Error: Invalid input: " + (if (inputText.length > maxChars) {
            "truncated inputStr(0, %d)".format(maxChars) + "=[" + inputText.substring(0, maxChars) + "]"
        } else {
          "[" + inputText + "]"
        }) + ", msg: " + ex.getMessage

        log.info(msg)
        throw new TacklerParseException(msg, ex)
    }
  }

  def txnsFile(inputPath: Path): TxnsContext = {
    try {
      parseTxns(CharStreams.fromPath(inputPath, Charset.forName("UTF-8")))
    } catch {
      case ex: ParseCancellationException =>
        val msg = "Txn Parse Error: [" + inputPath.toString + "] msg: " + ex.getMessage
        log.info(msg)
        throw new TacklerParseException(msg, ex)
    }
  }

  def txnsStream(inputStream: InputStream): TxnsContext = {
    try {
      parseTxns(CharStreams.fromStream(inputStream, Charset.forName("UTF-8")))
    } catch {
      case ex: ParseCancellationException =>
        val msg = "Txn Parse Error with input stream, msg: " + ex.getMessage
        log.info(msg)
        throw new TacklerParseException(msg, ex)
    }
  }

  protected def parseTxns(input: CharStream): TxnsContext = {
    val lexer = new TxnLexer(input)
    val tokens = new CommonTokenStream(lexer)
    val parser = new TxnParser(tokens)

    // try with simpler/faster SLL(*)
    parser.getInterpreter.setPredictionMode(PredictionMode.SLL)

    // we don't want error messages or recovery during first try
    parser.removeErrorListeners()
    parser.setErrorHandler(new BailErrorStrategy())

    try {
      parser.txns()
      // if we get here, there was no syntax error and SLL(*) was enough;
      // there is no need to try full LL(*)
    } catch {
      // thrown by BailErrorStrategy
      case _: ParseCancellationException =>
        log.debug("SLOW PATH")
        tokens.seek(0) // rewind input stream
        parser.reset()

        // Still using BailErrorStrategy as backup
        // TODO: Try to get rid off this listener and see if bailerror
        // has enough information
        parser.addErrorListener(TacklerErrorListener.INSTANCE)

        // full now with full LL(*)
        parser.getInterpreter.setPredictionMode(PredictionMode.LL)

        parser.txns()
    }
  }
}
