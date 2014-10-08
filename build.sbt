name := "akka-horde"

version := "1.0"

scalaVersion := "2.11.2"

lazy val root =
  project.in( file(".") )
    .aggregate(core, ui, zerg)

lazy val core = project

lazy val ui = project
  .dependsOn(core)

lazy val zerg = project
  .dependsOn(core)