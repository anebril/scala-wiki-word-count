package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.Word
import org.scalatest.FlatSpec

/**
  * Created by pavel on 6/11/16.
  */
class WikiWordCounterTest extends FlatSpec {

    behavior of "WikiWordCounterTest"

    val wordCounter = new WikiWordCounter

    it should "count words in czech example document" in {
        val text = scala.io.Source.fromFile("src/test/test-resources/example-text-czech.txt").mkString
        val words = wordCounter.countWords(text)
        assert(words != null)
        assert(words.nonEmpty)
    }

    it should "count words and compare with expectation" in {
        val text = "Lorem ipsum dolor sit amet, sit. Lorem-ipsum \n    \n lorem, ipsum, " +
          "sit AMET \n lorem-ipsum lorem"

        val words = wordCounter.countWords(text)
        assert(words != null)
        assert(words.nonEmpty)

        val expected = Set(
            Word("lorem", 5),
            Word("ipsum", 4),
            Word("sit", 3),
            Word("amet", 2),
            Word("dolor", 1)
        )

        assert(words.toSet == expected)
    }

    it should "count words in sentence with all czech characters" in {
        val text = "Nechť již hříšné saxofony ďáblů rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu."

        val words = wordCounter.countWords(text)
        assert(words != null)
        assert(words.nonEmpty)

        val expected = Set(
            Word("nechť", 1),
            Word("již", 1),
            Word("hříšné", 1),
            Word("saxofony", 1),
            Word("ďáblů", 1),
            Word("rozzvučí", 1),
            Word("síň", 1),
            Word("úděsnými", 1),
            Word("tóny", 1),
            Word("waltzu", 1),
            Word("tanga", 1),
            Word("a", 1),
            Word("quickstepu", 1)
        )

        assert(words.toSet == expected)
    }

    it should "count words in sentence with all czech characters capitalized" in {
        val text = "Nechť již hříšné saxofony ďáblů rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu.".toUpperCase()

        val words = wordCounter.countWords(text)
        assert(words != null)
        assert(words.nonEmpty)

        val expected = Set(
            Word("nechť", 1),
            Word("již", 1),
            Word("hříšné", 1),
            Word("saxofony", 1),
            Word("ďáblů", 1),
            Word("rozzvučí", 1),
            Word("síň", 1),
            Word("úděsnými", 1),
            Word("tóny", 1),
            Word("waltzu", 1),
            Word("tanga", 1),
            Word("a", 1),
            Word("quickstepu", 1)
        )

        assert(words.toSet == expected)
    }

}
