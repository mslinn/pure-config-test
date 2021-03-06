import java.nio.file.{Path, Paths}
import PureConfigFun._
import com.typesafe.config.ConfigRenderOptions
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint
import pureconfig.error._
import com.typesafe.config.ConfigValueType._
import pureconfig.ConfigReader.Result

// Build an assembly and run it:
// $ sbt assembly
// $ java -cp target/scala-2.13/pure-config-test-assembly-0.1.2.jar PureConfigTest
object PureConfigTest extends App {
  PureConfigFun.load match {
    case Right(right) => println(s"Success: $right")
      println(s"host = ${ right.http.host }") // FIXME host has embedded double quotes

    case Left(left) =>
      Console.err.println(s"Error: $left")
  }
}

// Build an assembly and run it:
// $ sbt assembly
// $ java -cp target/scala-2.13/pure-config-test-assembly-0.1.2.jar PureConfigTest2
object PureConfigTest2 extends App {
  try {
    val pureConfigFun: PureConfigFun = PureConfigFun.loadOrThrow
    Console.err.println(s"Success: $pureConfigFun")
    println(s"host = ${ pureConfigFun.http.host }")
  } catch {
    case e: Exception => Console.err.println(s"Error: $e")
  }
}

object PureConfigFun {
  import pureconfig.{CamelCase, ConfigConvert, ConfigFieldMapping}
  import pureconfig.error.ConfigReaderFailures
  import pureconfig.ConfigConvert._
  import com.typesafe.config.{ConfigValue, ConfigValueFactory, ConfigValueType}

  val defaultConsoleConfig: ConsoleConfig = ConsoleConfig()
  val defaultFeedConfig: FeedConfig = FeedConfig()
  val defaultHttpConfig: HttpConfig = HttpConfig()
  val defaultReplConfig: ReplConfig = ReplConfig()
  val defaultSpeciesConfig: SpeciesDefaults = SpeciesDefaults()
  val defaultSshServerConfig: SshServer = SshServer()

  /** Defined before `load` or `loadOrThrow` methods are defined so this implicit is in scope */
  implicit lazy val readHost: ConfigConvert[Host] = new ConfigConvert[Host] {
    override def from(config: ConfigValue): Either[ConfigReaderFailures, Host] = {
      config.valueType match {
        case ConfigValueType.STRING =>
          Right(Host(config.unwrapped.asInstanceOf[String]))

        case _ =>
          val wrongType: WrongType = WrongType(config.valueType, Set(ConfigValueType.STRING))
          val convertFailure: ConvertFailure = ConvertFailure(wrongType, ConfigCursor(config, List(ConfigValueLocation(config).toString)))
          Left(ConfigReaderFailures(convertFailure))
      }
    }

    override def to(host: Host): ConfigValue = ConfigValueFactory.fromAnyRef(host.value)

    // New required method for PureConfig 0.12.1
    override def from(cur: ConfigCursor): Result[Host] =
      if (cur.isUndefined) cur.asString.map(Host) else {
        val s = cur.value.unwrapped.toString
//        val Right(x) = cur.scopeFailure(catchReadError(_.toString)(implicitly)(s))
        cur.scopeFailure(catchReadError(_.toString)(implicitly)(s)).map(Host)
      }
  }

  /** Defined before `load` or `loadOrThrow` methods are defined so this implicit is in scope */
  implicit val readPort: ConfigConvert[Port] = new ConfigConvert[Port] {
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

  // This won't report an error about a missing file; instead it'll complain about missing config keys.
  lazy val confSource: ConfigSource ={
    val x = loadFrom("pure.conf") // I can see that host has embedded double quotes
    x
  }

  def loadFrom(confFileName: String): ConfigSource =
    ConfigSource
      .resources(confFileName)
      .withFallback(ConfigSource.file(confFileName).optional)
      .at("ew")

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
  def load: Either[ConfigReaderFailures, PureConfigFun] = confSource.load[PureConfigFun]

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
  def loadOrThrow: PureConfigFun = confSource.loadOrThrow[PureConfigFun]

  /** Be sure to define implicits such as [[ConfigConvert]] and [[ProductHint]] subtypes before this method so they are in scope */
  def apply: PureConfigFun = loadOrThrow
}

case class PureConfigFun(
  console: ConsoleConfig           = defaultConsoleConfig,
  feed: FeedConfig                 = defaultFeedConfig,
  http: HttpConfig                 = defaultHttpConfig,
  repl: ReplConfig                 = defaultReplConfig,
  speciesDefaults: SpeciesDefaults = defaultSpeciesConfig,
  sshServer: SshServer             = defaultSshServerConfig
)

case class FeedConfig(port: Port = Port(1100))

case class ConsoleConfig(enabled: Boolean = true)

case class HttpConfig(host: Host = Host("0.0.0.0"), port: Port = Port(5000))

case class Host(value: String) {
  override def toString: String = value
}

case class Port(value: Int)

case class ReplConfig(
  home: Path = Paths.get(System.getProperty("user.home"))
)

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
