DefaultOptions.addPluginResolvers
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")

addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")