cancelable := true

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """import java.nio.file.{Path, Paths}
                                |import PureConfigFun._
                                |import pureconfig.ProductHint
                                |import pureconfig.error.ConfigReaderFailures
                                |""".stripMargin

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

libraryDependencies ++= Seq(
  "com.github.pureconfig" %% "pureconfig"    % "0.8.0" withSources(), // Works: 0.7.0
  "org.scalatest"         %% "scalatest"     % "3.1.0" % Test withSources(),
  "junit"                 %  "junit"         % "4.12"  % Test
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

name := "pure-config-test"

organization := "com.micronautics"

version := "0.1.2"

scalaVersion := "2.12.10"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked"
)

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/changeMe/tree/master€{FILE_PATH}.scala"
  )
}.value

sublimeTransitive := true
