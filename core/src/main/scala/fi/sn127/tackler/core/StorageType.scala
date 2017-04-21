package fi.sn127.tackler.core

sealed trait StorageType
sealed case class GitStorageType() extends StorageType
sealed case class FilesystemStorageType() extends StorageType

object StorageType {
  def apply(backendType: String): StorageType = {
    backendType match {
      case "git" => GitStorageType()
      case "txn" => FilesystemStorageType()
      /* Error*/
      case bet => throw new ReportException(
        "Unknown backend type [" + bet + "]. Valid types are: git, txn")
    }
  }
}
