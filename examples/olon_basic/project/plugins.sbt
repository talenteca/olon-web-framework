resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.2.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

addSbtPlugin("com.twitter" %% "scrooge-sbt-plugin" % "23.11.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")
