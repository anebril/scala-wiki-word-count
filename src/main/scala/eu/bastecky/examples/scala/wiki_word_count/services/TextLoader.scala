package eu.bastecky.examples.scala.wiki_word_count.services

/**
  * Define methods for loading texts from text source. Text source can be either local (database) or remote (web page).
  */
trait TextLoader {

    /**
      * Loads text from a text source for given search query parameter. Method will send search query to a text source
      * and obtains matching text.
      *
      * Method can throw an exception if text source couldn't be connected.
      *
      * @param query Search query to be resolved as requested text
      * @return REquested text matching search query
      */
    def load(query: String): String
}

/**
  * Implementation of text loader which loads content of wikipedia article matching given search parameter. Loader uses
  * public wikipedia API to load articles.
  */
class WikiTextLoader(implicit config: Configuration) extends TextLoader {
    override def load(query: String): String = ???
}