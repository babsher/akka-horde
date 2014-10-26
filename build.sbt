import sbtassembly.Plugin.AssemblyKeys._

name := "akka-horde"

version := "1.0"

scalaVersion := "2.11.2"

lazy val root =
  project.in( file(".") )
    .aggregate(core, ui)

lazy val core = project

lazy val ui = project
  .dependsOn(core)

val akkaV = "2.3.6"
val sprayV = "1.3.2"

assemblySettings

lazy val buildSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.example",
  scalaVersion := "2.10.1"
)

val app = (project in file("app")).
  settings(buildSettings: _*).
  settings(assemblySettings: _*).
  settings(
    // your settings here
  )

val deployTask = TaskKey[Unit]("deploy", "Copies assembly jar to remote location")

deployTask <<= assembly map {
  (asm) =>
    IO.copyFile(asm, new File("/home/bryan/VirtualBox VMs/vm/share/akka-horde/" + asm.getName()))
}