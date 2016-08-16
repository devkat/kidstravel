// repository for Typesafe plugins
resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.11")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.3")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.3.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.github.sbtliquibase" % "sbt-liquibase" % "0.1.0-SNAPSHOT")