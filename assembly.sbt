test in assembly := {}

packageOptions in assembly ~= { pos =>
  pos.filterNot { po =>
    po.isInstanceOf[Package.MainClass]
  }
}

test in assembly := {}

//assemblyMergeStrategy in assembly := {
//  case x if /*x.startsWith("$") ||*/ x.endsWith(".conf") || x.endsWith(".properties") || x.endsWith(".json") => MergeStrategy.discard
//  case PathList("ammonite", xs @ _*) => MergeStrategy.discard
//  case PathList("derive", xs @ _*) => MergeStrategy.discard
//  case PathList("fansi", xs @ _*) => MergeStrategy.discard
//  case PathList("jline", xs @ _*) => MergeStrategy.discard
//  case PathList("geny", xs @ _*) => MergeStrategy.discard
//  case PathList("org", "apache", "sshd", xs @ _*) => MergeStrategy.discard
//  case PathList("org", "apache", "ivy", xs @ _*) => MergeStrategy.discard
//  case PathList("pprint", xs @ _*) => MergeStrategy.discard
//  case PathList("scalaparse", xs @ _*) => MergeStrategy.discard
//  case PathList("scopt", xs @ _*) => MergeStrategy.discard
//  case PathList("sourcecode", xs @ _*) => MergeStrategy.discard
//  case PathList("upickle", xs @ _*) => MergeStrategy.discard
//  case x => (assemblyMergeStrategy in assembly).value(x)
//}
