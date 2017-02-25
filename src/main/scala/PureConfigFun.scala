import java.nio.file.{Path, Paths}
import PureConfigFun._

object PureConfigTest extends App {
  val pureConfigFun = PureConfigFun.load
  println(pureConfigFun)
}

object PureConfigTest2 extends App {
  val pureConfigFun = PureConfigFun.loadOrThrow
  println(pureConfigFun)
}

object PureConfigFun {
  import pureconfig.error.ConfigReaderFailures
  import pureconfig.ProductHint

  /** Supposed to fail if an unknown key is found, but causes the 2 load methods below to fail with
    * pureconfig.error.ConfigReaderException: Cannot convert configuration to a value of class PureConfigFun. Failures are:
  Other failures:
    -  UnknownKey(path,None)
    -  UnknownKey(file,None)
    -  UnknownKey(java,None)
    -  UnknownKey(os,None)
    -  UnknownKey(line,None)
    -  UnknownKey(user,None)
    -  UnknownKey(sun,None)
    -  UnknownKey(akka,None)
    -  UnknownKey(awt,None)
    * @see https://github.com/melrief/pureconfig/blob/master/core/docs/override-behaviour-for-case-classes.md#unknown-keys */
  //implicit val hint: ProductHint[PureConfigFun] = ProductHint[PureConfigFun](allowUnknownKeys = false)

  def load: Either[ConfigReaderFailures, PureConfigFun] = pureconfig.loadConfig[PureConfigFun](Paths.get("pure.conf"))

  def loadOrThrow: PureConfigFun = pureconfig.loadConfigOrThrow[PureConfigFun](Paths.get("pure.conf"))

  def apply: PureConfigFun = loadOrThrow

  val defaultConsoleConfig   = ConsoleConfig()
  val defaultFeedConfig      = FeedConfig()
  val defaultReplConfig      = ReplConfig()
  val defaultSpeciesConfig   = SpeciesConfig()
  val defaultSshServerConfig = SshServerConfig()
}

case class PureConfigFun(
  console: ConsoleConfig         = defaultConsoleConfig,
  feed: FeedConfig               = defaultFeedConfig,
  repl: ReplConfig               = defaultReplConfig,
  speciesDefaults: SpeciesConfig = defaultSpeciesConfig,
  sshServer: SshServerConfig     = defaultSshServerConfig
)

case class FeedConfig(port: Port = Port(1100))

case class ConsoleConfig(enabled: Boolean = true) extends AnyVal

case class Port(value: Int) extends AnyVal

case class ReplConfig(
  home: Path = Paths.get(System.getProperty("user.home"))
) extends AnyVal

case class SpeciesConfig(
  attributeMinimum: Int = 0,
  attributeMaximum: Int = 100,
  eventQLength: Int = 25,
  historyLength: Int = 20
)

case class SshServerConfig(
  enabled: Boolean = true,
  password: String = "",
  port: Port = Port(1101)
)
