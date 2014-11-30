import AssemblyKeys._ // put this at the top of the file


name := "akka-horde"

version := "1.0"

scalaVersion := "2.11.2"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"

classpathTypes ++= Set("dll")

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

val akkaV = "2.3.6"
val sprayV = "1.3.2"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "ch.qos.logback" % "logback-core" % "1.1.2",
  "io.spray" %% "spray-json" % "1.3.0",
  "io.spray" %% "spray-can" % sprayV,
  "io.spray" %% "spray-routing" % sprayV,
  "com.typesafe.akka" %% "akka-actor" % akkaV,
  "com.typesafe.akka" %% "akka-kernel" % akkaV,
  "com.typesafe.akka" %% "akka-slf4j" % akkaV,
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.11",
  "com.google.guava" % "guava" % "18.0",
  "junit" % "junit" % "4.8.1" % "test",
  "io.spray" %% "spray-testkit" % sprayV % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

assemblySettings

mainClass in assembly := Some("edu.gmu.horde.Boot")

val deployTask = TaskKey[Unit]("deploy", "Copies assembly jar to remote location")

deployTask <<= assembly map {
  (asm) =>
    IO.copyFile(asm, new File("/home/bryan/VirtualBox VMs/vm/share/akka-horde/" + asm.getName()))
}
