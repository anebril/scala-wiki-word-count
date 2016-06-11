package eu.bastecky.examples.scala.wiki_word_count.beans

/**
  * Represents single word with number of occurrences in text
  *
  * @param word Word from text
  * @param count Number of occurrences of the word in text
  */
case class Word(word: String, count: Int)
