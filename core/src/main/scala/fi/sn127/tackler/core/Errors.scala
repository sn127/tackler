package fi.sn127.tackler.core

class TacklerException(val message: String) extends Exception(message)

class AccountException(message: String) extends TacklerException(message)

class TxnException(message: String) extends TacklerException(message)

class GroupByException(message: String) extends TacklerException(message)

class ReportException(message: String) extends TacklerException(message)

