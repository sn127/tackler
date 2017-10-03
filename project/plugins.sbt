// code generators
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")
addSbtPlugin("com.simplytyped" % "sbt-antlr4" % "0.8.0")

// build & release
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

// QA tools
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.2")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1")
