package eu.bastecky.examples.scala.wiki_word_count.services

import java.util.Properties
import java.io.InputStream

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
class Configuration {

    /** URI of wikipedia api endpoint for loading texts */
    val WikiEndpointProperty = "wiki.endpoint"
    val WikiQueryParamProperty="wiki.query.param"

    /** Name of file with default properties */
    val ConfigFileName = "config.properties"

    val properties = new Properties()
    loadFile(ConfigFileName)

    /**
      * Gets string value for given key. Throws IllegalStateException if property is not set.
      */
    def getValue(key: String): String = {

        if (System.getProperties.keySet().contains(key)) {
            System.getProperty(key)
        }
        else if (properties.keySet().contains(key)) {
            properties.getProperty(key)
        }
        else {
            throw new IllegalStateException(s"Missing required property: $key in application config property file")
        }
    }

    /**
      * Loads given file into collection of properties.
      * @param fileName Path to file in classpath
      */
    def loadFile(fileName: String) = {
        var input: Option[InputStream] = None

        try {
            input = Option(getClass.getClassLoader.getResourceAsStream(fileName))
            if (input.isDefined) {
                properties.load(input.get)
            }
        }
        finally {
            if (input.isDefined) {
                input.get.close()
            }
        }
    }
}
