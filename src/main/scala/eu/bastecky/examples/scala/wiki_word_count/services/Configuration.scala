package eu.bastecky.examples.scala.wiki_word_count.services

import java.util.Properties
import java.io.InputStream

import org.slf4j.LoggerFactory

object Configuration {
    val TextSourceProperty = "text.source"

    /** URI of wikipedia api endpoint for loading texts */
    val WikiEndpointProperty = "wiki.endpoint"
    val WikiQueryParamProperty = "wiki.query.param"
    val WikiProtocolProperty = "wiki.protocol"
    val DerbyDatabaseNameProperty = "derby.database.name"
    val DerbyDatabaseIsMemoryProperty = "derby.database.isMemory"

    /** Set of values which are resolved to FALSE - any other value is resolved to true **/
    val FalseValues = Set("false", "0", "")
}

/**
  * Represents collection of configuration properties for this application. Configuration is represented as set of
  * key/value properties. Each property is some string value for some string key.
  */
trait Configuration {

    import Configuration._

    /**
      * Sets given value to a property with given key.
      */
    def setValue(key: String, value: String)

    /**
      * Gets string value for given key. Returns None if property is not set.
      */
    def getOptionalValue(key: String): Option[String]

    /**
      * Gets string value for given key. Throws IllegalStateException if property is not set.
      */
    def getValue(key: String): String = {
        val value = getOptionalValue(key)

        if (value.isDefined) value.get
        else throw new IllegalStateException(s"Missing required property: $key in application config property file")
    }

    /**
      * Gets boolean value for given key. Returns False is property is not set or is set to "false", "0", "". Otherwise
      * returns true.
      */
    def getBoolValue(key: String): Boolean = {
        val value = getOptionalValue(key)

        if (value.isDefined) !FalseValues.contains(value.get.toLowerCase)
        else false
    }
}

/**
  * Represents collection of configuration properties for this application. Configuration is represented as set of
  * key/value properties. Each property is some string value for some string key.
  *
  * Properties are obtained from system properties or from main configuration file (property file in classpath). System
  * properties has higher priority and can be set as commandline parameters using java D option. Config file has second
  * priority and contains defaults for all properties.
  *
  * File with properties is loaded in constructor of this class.
  *
  * Option to set configuration property:
  *         -Dproperty.name=property-value
  */
class PropertyConfiguration extends Configuration {

    val logger = LoggerFactory.getLogger(classOf[PropertyConfiguration])

    /** Name of file with default properties */
    val ConfigFileName = "config.properties"

    val properties = new Properties()
    loadFile(ConfigFileName)

    /**
      * Sets given value to a property with given key.
      */
    override def setValue(key: String, value: String): Unit = properties.setProperty _

    /**
      * Gets string value for given key. Returns None if property is not set.
      */
    override def getOptionalValue(key: String): Option[String] = {
        logger trace s"Resolving value of property: $key"

        if (System.getProperties.keySet().contains(key)) Some(System.getProperty(key))
        else if (properties.keySet().contains(key)) Some(properties.getProperty(key))
        else None
    }

    /**
      * Loads given file into collection of properties.
      *
      * @param fileName Path to file in classpath
      */
    def loadFile(fileName: String) = {

        logger debug s"Loading file with configuration properties: $fileName"

        var input: Option[InputStream] = None

        try {
            input = Option(getClass.getClassLoader.getResourceAsStream(fileName))
            if (input.isDefined) {
                properties.load(input.get)
                logger debug s"Properties has been loaded"
            }
            else {
                logger warn "Unable to load file with properties"
            }
        }
        finally {
            if (input.isDefined) input.get.close()
        }
    }

}
