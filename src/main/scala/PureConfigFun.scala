import java.nio.file.{Path, Paths}
import PureConfigFun._
import com.typesafe.config.{ConfigValue, ConfigValueFactory, ConfigValueType}
import pureconfig.{CamelCase, ConfigConvert, ConfigFieldMapping}
import pureconfig.ConfigConvert._
import pureconfig.error.{CannotConvert, ConfigValueLocation}

object PureConfigTest extends App {
  val pureConfigFun = PureConfigFun.load
  println(pureConfigFun)
}

object PureConfigTest2 extends App {
  val pureConfigFun = PureConfigFun.loadOrThrow
  println(pureConfigFun)
}

object PureConfigFun {
  import pureconfig.ProductHint
  import pureconfig.error.ConfigReaderFailures

  val defaultConsoleConfig   = ConsoleConfig()
  val defaultFeedConfig      = FeedConfig()
  val defaultReplConfig      = ReplConfig()
  val defaultSpeciesConfig   = SpeciesDefaults()
  val defaultSshServerConfig = SshServer()

  implicit val readPort = new ConfigConvert[Port] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Port] = {
      config.valueType() match {
        case ConfigValueType.NUMBER =>
          Right(Port(config.unwrapped().asInstanceOf[Int]))

        case t =>
          fail(CannotConvert(config.render(), "Port", s"A port should be a number, but ${ config.valueType } was found", ConfigValueLocation(config)))
      }
    }

    override def to(port: Port): ConfigValue = ConfigValueFactory.fromAnyRef(port.value)
  }

  /** Fail if an unknown key is found.
    * @see https://github.com/melrief/pureconfig/blob/master/core/docs/override-behaviour-for-case-classes.md#unknown-keys
    *
    * Support CamelCase
    * @see https://github.com/melrief/pureconfig/blob/master/core/docs/override-behaviour-for-case-classes.md#override-behaviour-for-case-classes */
  implicit val hint: ProductHint[PureConfigFun] = ProductHint[PureConfigFun](
    allowUnknownKeys = false,
    fieldMapping = ConfigFieldMapping(CamelCase, CamelCase)
  )

  lazy val confPath: Path = new java.io.File(getClass.getResource("pure.conf").getPath).toPath

  def load: Either[ConfigReaderFailures, PureConfigFun] = pureconfig.loadConfig[PureConfigFun](confPath, "ew")

  def loadOrThrow: PureConfigFun = pureconfig.loadConfigOrThrow[PureConfigFun](confPath, "ew")

  def apply: PureConfigFun = loadOrThrow
}

case class PureConfigFun(
  console: ConsoleConfig           = defaultConsoleConfig,
  feed: FeedConfig                 = defaultFeedConfig,
  repl: ReplConfig                 = defaultReplConfig,
  speciesDefaults: SpeciesDefaults = defaultSpeciesConfig,
  sshServer: SshServer             = defaultSshServerConfig
)

case class FeedConfig(port: Port = Port(1100))

case class ConsoleConfig(enabled: Boolean = true) extends AnyVal

case class Port(value: Int) extends AnyVal

case class ReplConfig(
  home: Path = Paths.get(System.getProperty("user.home"))
) extends AnyVal

case class SpeciesDefaults(
  attributeMinimum: Int = 0,
  attributeMaximum: Int = 100,
  eventQLength: Int = 25,
  historyLength: Int = 20
)

case class SshServer(
  address: String = "localhost",
  ammoniteHome: Path = Paths.get(System.getProperty("user.home")),
  enabled: Boolean = true,
  hostKeyFile: Option[Path] = None, //Some(Paths.get(System.getProperty("user.home") + ".ssh/id_rsa")),
  password: String = "",
  port: Port = Port(1101),
  userName: String = "repl"
)
