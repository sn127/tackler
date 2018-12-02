/*
 * Copyright 2016-2018 SN127.fi
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

class TacklerException(val message: String) extends Exception(message)

class AccountException(message: String) extends TacklerException(message)

class CommodityException(message: String) extends TacklerException(message)

class TxnException(message: String) extends TacklerException(message)

class GroupByException(message: String) extends TacklerException(message)

class ReportException(message: String) extends TacklerException(message)

class ExportException(message: String) extends TacklerException(message)
