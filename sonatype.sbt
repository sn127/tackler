sonatypeProfileName := "fi.sn127"

publishMavenStyle := true

inThisBuild(List(
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
homepage := Some(url("https://github.com/sn127/tackler")),
scmInfo := Some(
  ScmInfo(
    url("https://github.com/sn127/tackler"),
    "scm:git:https://github.com/sn127/tackler.git"
  )
),
developers := List(
  Developer(id="sn127", name="SN127", email="dev@sn127.fi", url=url("https://github.com/sn127"))
)
))
