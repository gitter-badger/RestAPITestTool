logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("public")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.0")