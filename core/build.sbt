import Dependencies._


libraryDependencies += betterFiles
libraryDependencies += cats_core
libraryDependencies ++= circe_deps

libraryDependencies += typesafeConfig
libraryDependencies += jgit
libraryDependencies += scalaArm

libraryDependencies += scalatest % "test"
libraryDependencies ++= circe_deps_test
