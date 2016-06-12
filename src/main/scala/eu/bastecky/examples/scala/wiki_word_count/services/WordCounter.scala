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

    /** Regular expression defining words - non empty sequence of letters in english or other languages */
    val reg = "([\\u00BF-\\u1FFF\\u2C00-\\uD7FF\\w])+".r

    override def countWords(text: String): Seq[Word] =
        reg.findAllIn(text)                     // Identify all words in given text and resolves sequence
          .toSeq
          .map(_.toLowerCase())                 // Normalizes words - converts them to lowercase
          .groupBy(w => w)                      // Group words - sequence of (String, Seq[String])
          .map(w => Word(w._1, w._2.size))      // For each word group creates Word object with word and size of group
          .toSeq
          .sortWith(_.count > _.count)          // Sort by number of occurrences (most common words at top)
}
