import AssemblyKeys._

name := "akka-horde"

version := "1.1"

scalaVersion := "2.11.5"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"

EclipseKeys.withSource := true

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

val akkaV = "2.3.9"
val akkaStreamV = "1.0-M3"
val sprayV = "1.3.2"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "ch.qos.logback" % "logback-core" % "1.1.2",
  "com.typesafe.akka" %% "akka-stream-experimental"          % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-core-experimental"       % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-experimental"            % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaStreamV,
  "com.typesafe.akka" %% "akka-actor"   % akkaV,
  "com.typesafe.akka" %% "akka-kernel"  % akkaV,
  "com.typesafe.akka" %% "akka-slf4j"   % akkaV,
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.11",
  "com.google.guava" % "guava" % "18.0",
  "junit" % "junit" % "4.8.1" % "test",
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

assemblySettings

mainClass in assembly := Some("edu.gmu.horde.actors.HttpService")

val deployTask = TaskKey[Unit]("deploy", "Copies assembly jar to remote location")

deployTask <<= assembly map {
  (asm) =>
    IO.copyFile(asm, new File("/home/bryan/VirtualBox VMs/vm/share/akka-horde/" + asm.getName()))
}
