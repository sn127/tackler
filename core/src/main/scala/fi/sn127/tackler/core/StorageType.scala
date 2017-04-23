package fi.sn127.tackler.core

sealed trait StorageType
sealed case class GitStorageType() extends StorageType
sealed case class FilesystemStorageType() extends StorageType

object StorageType {
  def apply(storageType: String): StorageType = {
    storageType match {
      case "git" => GitStorageType()
      case "fs" => FilesystemStorageType()
      /* Error*/
      case storage => throw new TacklerException(
        "Unknown storage type [" + storage + "]. Valid types are: git, fs")
    }
  }
}
