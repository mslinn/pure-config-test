organization := "com.micronautics"

name := "pure-config-test"

version := "0.1.0"

scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlint"
)

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/changeMe/tree/master€{FILE_PATH}.scala"
  )
}.value

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

lazy val ammVer  = "0.8.2"

libraryDependencies ++= Seq(
  "com.lihaoyi"        %  "ammonite"      % ammVer  withSources() cross CrossVersion.full,
  "com.lihaoyi"        %  "ammonite-sshd" % ammVer  withSources() cross CrossVersion.full,
  "com.github.melrief" %% "pureconfig"    % "0.6.0" withSources(),
  "org.scalatest"      %% "scalatest"     % "3.0.1" % "test" withSources(),
  "junit"              %  "junit"         % "4.12"  % "test"
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """import java.nio.file.{Path, Paths}
                                |import PureConfigFun._
                                |import pureconfig.ProductHint
                                |import pureconfig.error.ConfigReaderFailures
                                |""".stripMargin

cancelable := true

sublimeTransitive := true
