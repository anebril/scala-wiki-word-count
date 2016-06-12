package eu.bastecky.examples.scala.wiki_word_count.beans

/**
  * Represents text entry loaded from a text source.
  *
  * @param textSource Identification of used text source
  * @param query Search query which was used to load text within this entry
  * @param text Original text loaded from a text source
  * @param words List of words of loaded text with number of occurrences
  */
case class TextEntry(textSource: String, query: String, text: String, words: Seq[Word])
