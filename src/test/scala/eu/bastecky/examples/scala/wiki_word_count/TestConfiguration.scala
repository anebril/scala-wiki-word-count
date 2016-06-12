package eu.bastecky.examples.scala.wiki_word_count

import eu.bastecky.examples.scala.wiki_word_count.services.Configuration

/**
  * Created by pavel on 6/12/16.
  */
class TestConfiguration(config: Map[String, String]) extends Configuration {
    /**
      * Sets given value to a property with given key.
      */
    override def setValue(key: String, value: String): Unit =
        throw new UnsupportedOperationException("Test configuration doesn't support setting properties")

    /**
      * Gets string value for given key. Returns None if property is not set.
      */
    override def getOptionalValue(key: String): Option[String] = config.get(key)
}
