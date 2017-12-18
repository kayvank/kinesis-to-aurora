import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.SettingsHelper._

organization := "q2io"

name := "user-likes-svc"

scalaVersion := "2.11.8"

import TodoListPlugin._

compileWithTodolistSettings

testWithTodolistSettings

lazy val root = (project in file("."))
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    JavaAppPackaging,
    DockerPlugin,
    UniversalPlugin).settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      buildInfoBuildNumber),
    buildInfoPackage := "info",
    buildInfoOptions ++= Seq(BuildInfoOption.BuildTime, BuildInfoOption.ToJson)
  )

mainClass in (Compile) := Some("Bootstrap")

mappings in Universal ++= Seq(
  findJarFromUpdate("aspectjweaver", update.value) ->
    "aspectj/aspectjweaver.jar"
)

scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-encoding", "UTF-8",
  "-feature",
  "-Xlog-reflective-calls",
  "-Ywarn-unused",
  //"-Ylog-classpath", // show me the classpath
  "-Ywarn-value-discard",
  "-Xlint",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xfuture",
  "-language:postfixOps",
  "-language:implicitConversions"
)

resolvers ++= Seq(
   Resolver.sonatypeRepo("snapshots"),
  "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= {
  object V {
    val specs2 = "3.7"
    val kamon = "0.6.1"
    val scalacheck = "1.13.2"
    val http4s = "0.15.0a"
    val circe = "0.6.1"
    val doobie = "0.3.0"
    val awsSdk = "1.9.23"
    val kcl = "1.6.5"
    val mysql = "5.1.39"
  }
  Seq(
    "com.amazonaws" % "amazon-kinesis-client" % V.kcl,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,
    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-circe" % V.http4s,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe,
    "io.circe" %% "circe-optics" % V.circe,
    "org.tpolecat"   %% "doobie-core" % V.doobie,
    "org.tpolecat"   %% "doobie-contrib-hikari" % V.doobie,
    "org.tpolecat"   %% "doobie-contrib-specs2" % V.doobie,
    "org.tpolecat" %% "doobie-contrib-postgresql" % V.doobie,
    "mysql" % "mysql-connector-java" % V.mysql,
    "ch.qos.logback" % "logback-classic" % "1+",
    "com.typesafe.scala-logging" %% "scala-logging" % "3+",
    "com.typesafe" % "config" % "1.2.1",
    "org.specs2" %% "specs2-core" % V.specs2 % "test",
    "org.specs2" %% "specs2-scalacheck" % V.specs2 % "test",
    "com.h2database" % "h2" % "1.3.175" % "test",
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % "test",
    "com.lihaoyi" % "ammonite" % "0.8.0" % "test" cross CrossVersion.full,
    "io.kamon" %% "kamon-core" % V.kamon
      exclude("com.typesafe.akka", "akka-actor_2.11"),
    "io.kamon" %% "kamon-datadog" % V.kamon,
    "io.kamon" %% "kamon-statsd" % V.kamon,
    "io.kamon" %% "kamon-log-reporter" % V.kamon,
    "io.kamon" %% "kamon-system-metrics" % V.kamon,
    "org.aspectj" % "aspectjweaver" % "1.8.9"
)}

parallelExecution in Test := false

buildInfoKeys += buildInfoBuildNumber

buildInfoOptions += BuildInfoOption.BuildTime

publishMavenStyle := true

dockerExposedPorts := Seq(9000, 9443)

maintainer in Docker := "admin@q2io.com"

version in Docker := version.value +
  "-b" + sys.props.getOrElse("build_number", default = "dev")

dockerRepository := Some("q2io")

dockerCommands ++= Seq(
)

dockerUpdateLatest := true

Seq(bintrayResolverSettings: _*)

deploymentSettings

publish <<= publish.dependsOn(publish in config("universal"))

javaOptions += "-Xmx512m"

aspectjSettings

fork in run := true // required for kamon

fork in test := false // required for kamon

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

javaOptions in Universal ++= Seq(
  "-J-Xms256m",
  "-J-Xmx512m"
)

bashScriptExtraDefines ++= Seq("addJava -javaagent:${app_home}/../aspectj/aspectjweaver.jar")

javaOptions in Universal += s"-Dkamon.auto-start=true"

def findJarFromUpdate(jarName: String, report: UpdateReport): File = {
  val filter = artifactFilter(name = jarName + "*", extension = "jar")
  val matches = report.matching(filter)
  if (matches.isEmpty) {
    val err: (String => Unit) = System.err.println
    err("can’t find jar file in resources named " + jarName)
    err("unfiltered jar list:")
    report.matching(artifactFilter(extension = "jar")).foreach(x => err(x.toString))
    throw new ResourcesException("can’t find jar file in resources named " + jarName)
  } else {
    matches.head
  }
}
