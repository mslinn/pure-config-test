import java.nio.file.{Path, Paths}
import PureConfigFun._
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint
import pureconfig.error._
import com.typesafe.config.ConfigValueType._
import pureconfig.ConfigReader.Result

object PureConfigTest extends App {
  val pureConfigFun = PureConfigFun.load
  println(pureConfigFun)
}

object PureConfigTest2 extends App {
  val pureConfigFun = PureConfigFun.loadOrThrow
  println(pureConfigFun)
}

object PureConfigFun {
  import pureconfig.{CamelCase, ConfigConvert, ConfigFieldMapping}
  import pureconfig.error.ConfigReaderFailures
  import pureconfig.ConfigConvert._
  import com.typesafe.config.{ConfigValue, ConfigValueFactory, ConfigValueType}

  val defaultConsoleConfig: ConsoleConfig = ConsoleConfig()
  val defaultFeedConfig: FeedConfig = FeedConfig()
  val defaultReplConfig: ReplConfig = ReplConfig()
  val defaultSpeciesConfig: SpeciesDefaults = SpeciesDefaults()
  val defaultSshServerConfig: SshServer = SshServer()

  /** Define before `load` or `loadOrThrow` methods are defined so this implicit is in scope */
  implicit val readPort: ConfigConvert[Port] {
    def to(port: Port): ConfigValue

    def from(config: ConfigValue): Either[ConfigReaderFailures, Port]
  } = new ConfigConvert[Port] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Port] = {
      config.valueType match {
        case ConfigValueType.NUMBER =>
          Right(Port(config.unwrapped.asInstanceOf[Int]))

        case _ =>
          // TODO This provides less information that the old style error handling, immediately below
          // Is there a better way to write this?
          Left(ConfigReaderFailures(ConvertFailure(WrongType(STRING, Set(OBJECT)), None, config.render)))

          // FYI, For PureConfig 0.7.0, this code was:
//          fail(CannotConvert(
//            value = config.render,
//            toType = "Port",
//            because = s"A port should be a number, but ${ config.valueType } was found",
//            location = ConfigValueLocation(config),
//            path = None
//          ))
      }
    }

    override def to(port: Port): ConfigValue = ConfigValueFactory.fromAnyRef(port.value)

    // New required method for PureConfig 0.12.1
    override def from(cur: ConfigCursor): Result[Port] =
      if (cur.isUndefined) cur.asInt.map(Port) else {
        val s = cur.value.render(ConfigRenderOptions.concise)
        cur.scopeFailure(catchReadError(_.toInt)(implicitly)(s)).map(Port)
      }
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

  lazy val confPath: Path = new java.io.File(getClass.getClassLoader.getResource("pure.conf").getPath).toPath

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
  // TODO how to rewrite this without invoking the deprecated loadConfig method?
  def load: Either[ConfigReaderFailures, PureConfigFun] = pureconfig.loadConfig[PureConfigFun](confPath, "ew")

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
  // TODO how to rewrite this without invoking the deprecated loadConfigOrThrow method?
  // FIXME this worked with PureConfig 0.7.0, but fails on 0.12.1 with:
  // [error] pureconfig.error.ConfigReaderException: Cannot convert configuration to a PureConfigFun. Failures are:
  //[error]   - Unable to read file file:/tmp/sbt_9da3b7/job-2/target/2d0db0fc/821842a3/pure-config-test_2.12-0.1.2.jar!/pure.conf (No such file or directory).
  def loadOrThrow: PureConfigFun = pureconfig.loadConfigOrThrow[PureConfigFun](confPath, "ew")

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
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
  ammoniteHome: Path = Paths.get(System.getProperty("user.home") + "/.ammonite"),
  enabled: Boolean = true,
  hostKeyFile: Option[Path] = None,
  password: String = "",
  port: Port = Port(1101),
  userName: String = "repl"
)
