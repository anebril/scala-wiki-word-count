package eu.bastecky.examples.scala.wiki_word_count.services

import org.scalatest.FlatSpec

/**
  * Created by pavel on 6/11/16.
  */
class ConfigurationTest extends FlatSpec {

    behavior of "ConfigurationTest"

    it should "get value from configuration file" in {
        val config = new Configuration

        val value = config.getValue(config.WikiEndpointProperty)
        assert(value != null)

        System.setProperty("test.property", "test-value")
        val systemValue = config.getValue("test.property")
        assert(systemValue == "test-value")

        intercept[IllegalStateException] {
            config.getValue("not.exists")
        }
    }
}
