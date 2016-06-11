package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.TextEntry

/**
  * Defines methods of persistence layer for caching processed TextEntries.
  */
trait Persistence {

    /**
      * Loads stored TextEntry with query value matching given parameter. None value is returned if TextEntry is missing
      * for given database.
      *
      * @param query Search query which uniquely identifies required TextEntry object
      * @return Stored TExtEntry object or None if there is no persisted value.
      */
    def getTextEntry(query: String): Option[TextEntry]

    /**
      * Store given text entry into persistence layer. Stored cache entry can be loaded back by its query parameter
      * using method getTextEntry.
      *
      * Important: TextEntry is uniquely identified by its query parameter. New entry with the same query will override
      * the previous one.
      *
      * @param entry TextEntry to be persisted in persistence layer.
      */
    def storeTextEntry(entry: TextEntry)
}

/**
  * Implementation of persistence based on runtime DERBY database.
  */
class DerbyPersistence extends Persistence {
    override def getTextEntry(query: String): Option[TextEntry] = ???

    override def storeTextEntry(entry: TextEntry): Unit = ???
}

