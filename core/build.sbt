name := "akka-horde-core"

version := "1.0"

scalaVersion := "2.11.2"

classpathTypes ++= Set("dll")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

val akkaV = "2.3.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-kernel" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "org.slf4j" % "slf4j-simple" % "1.7.7",
  "junit" % "junit" % "4.8.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

