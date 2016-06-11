package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.Word

/**
  * Defines method for processing loaded texts into collection of words and number of its occurrences.
  */
trait WordCounter {
    /**
      * Identify and count words in given text. Text will be cleared to list of simple words (by removing special
      * characters) and these words will be counted.
      *
      * @param text Text to be processed
      * @return Collection of words in text and number of occurrences
      */
    def countWords(text: String): Seq[Word]
}

/**
  * Implementation of word counter for processing texts from wikipedia text source.
  */
class WikiWordCounter extends WordCounter {
    override def countWords(text: String): Seq[Word] = ???
}
