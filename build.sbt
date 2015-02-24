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
  "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaStreamV % "test",
  "com.typesafe.akka" %% "akka-actor"   % akkaV,
  "com.typesafe.akka" %% "akka-kernel"  % akkaV,
  "com.typesafe.akka" %% "akka-slf4j"   % akkaV,
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.11",
  "com.google.guava" % "guava" % "18.0",
  "junit" % "junit" % "4.12" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

assemblySettings

mergeStrategy in assembly := {
  case PathList("app", "node_modules", xs @ _*)      => MergeStrategy.discard
  case PathList("app", "gulp", xs @ _*)              => MergeStrategy.discard
  case PathList("app", "src", xs @ _*)               => MergeStrategy.discard
  case PathList("app", "bower_components", xs @ _*)  => MergeStrategy.discard
  case PathList("app", "e2e", xs @ _*)               => MergeStrategy.discard
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("edu.gmu.horde.Boot")

lazy val deployTask = TaskKey[Unit]("deploy", "Copies assembly jar to remote location")

deployTask <<= assembly map {
  (asm) =>
    IO.copyFile(asm, new File("/home/bryan/VirtualBox VMs/vm/share/akka-horde/" + asm.getName()))
}

lazy val execScript = taskKey[Unit]("Execute the build script")

execScript := {
  "./build.sh" !
}

compile <<= (compile in Compile) dependsOn execScript
