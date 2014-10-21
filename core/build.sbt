name := "akka-horde-core"

version := "1.0"

scalaVersion := "2.11.2"

classpathTypes ++= Set("dll")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.akka" %% "akka-kernel" % "2.3.6",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.6",
  "org.slf4j" % "slf4j-simple" % "1.7.7",
  "junit" % "junit" % "4.8.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)