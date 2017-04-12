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
package fi.sn127.tackler.model

import org.scalatest.FlatSpec

import fi.sn127.tackler.core.AccountException

class AccountTreeNodeTest extends FlatSpec {

  behavior of "AccountTreeNodeTest"

  it should "apply with deep leaf-account" in {
    val atn: AccountTreeNode = AccountTreeNode("a:b:c")

    assert(atn.depth === 3)
    assert(atn.root === "a")
    assert(atn.parent === "a:b")
    assert(atn.account === "a:b:c")
    assert(atn.name === "c")
  }

  it should "apply with root-account" in {
    val atn: AccountTreeNode = AccountTreeNode("a")

    assert(atn.depth === 1)
    assert(atn.root === "a")
    assert(atn.parent === "")
    assert(atn.account === "a")
    assert(atn.name === "a")
  }

  it should "not work with an empty account" in {

    assertThrows[AccountException]{
      AccountTreeNode("")
    }
  }

  it should "not work with empty sub-components account, 1" in {

    assertThrows[AccountException]{
      AccountTreeNode(":")
    }
  }

  it should "not work with empty sub-components accounts, 2" in {

    assertThrows[AccountException]{
      AccountTreeNode(": :")
    }
  }
}
