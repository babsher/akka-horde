name := "akka-horde-ui"

version := "1.0"

scalaVersion := "2.11.2"

val akkaV = "2.3.6"
val sprayV = "1.3.2"

libraryDependencies ++= Seq(
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test"
)
