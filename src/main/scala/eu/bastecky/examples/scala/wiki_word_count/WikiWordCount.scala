package eu.bastecky.examples.scala.wiki_word_count

import eu.bastecky.examples.scala.wiki_word_count.beans.TextEntry
import eu.bastecky.examples.scala.wiki_word_count.services._

/**
  * Created by pavel on 6/8/16.
  */
object WikiWordCount {

    implicit lazy val config = new Configuration
    implicit lazy val persistence: Persistence = new DerbyPersistence
    implicit lazy val textLoader: TextLoader = new WikiTextLoader
    implicit lazy val wordCounter: WordCounter = new WikiWordCounter

    implicit lazy val wikiWordCount = new WikiWordCount

    def main(args: Array[String]) = wikiWordCount.main _
}

/**
  *
  * @param persistence
  * @param textLoader
  * @param wordCounter
  */
class WikiWordCount(
                   implicit
                   persistence: Persistence,
                   textLoader: TextLoader,
                   wordCounter: WordCounter
                   ) {

    def main(args: Array[String]) {

    }

    def getEntry(query: String): TextEntry = {

        val persisted = persistence.getTextEntry(query)

        if (persisted.isEmpty) {
            val text = textLoader.load(query)
            val words = wordCounter.countWords(text)

            val textEntry = TextEntry(query, text, words)

            persistence.storeTextEntry(textEntry)
            textEntry
        }
        else {
            persisted.get
        }
    }
}
