package eu.bastecky.examples.scala.wiki_word_count.beans

/**
  * Created by pavel on 6/11/16.
  */
class WordCountException(message: String, e: Throwable) extends Exception(message, e) {
    def this(message: String) = this(message, null)
}
