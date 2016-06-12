package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.WordCountException
import org.scalatest.FlatSpec

/**
  * Created by pavel on 6/11/16.
  */
class WikiTextLoaderTest extends FlatSpec {

    behavior of "WikiTextLoaderTest"

    val config = new PropertyConfiguration
    config.setValue(Configuration.TextSourceProperty, "cs.wikipedia.org")

    val loader = new WikiTextLoader()(config)

    val TestQuery = "Scala (programovací jazyk)"

    it should "load text from wiki and extract text" in {
        val text = loader.load(TestQuery)
        assert(text != null)
        assert(text.nonEmpty)
    }

    it should "load text from wiki using configuration" in {

        val text = loader.performRequest(TestQuery, loader.textSource)
        assert(text != null)
        assert(text.nonEmpty)
    }

    it should "fail loading text from wrong uri" in {

        intercept[WordCountException] {
            val text = loader.performRequest(TestQuery, "dontexists.dontexists")
        }

        intercept[WordCountException] {
            val text = loader.performRequest(TestQuery, "wrong")
        }
    }

    it should "extract wiki text from example response" in {
        val response = scala.io.Source.fromFile("src/test/test-resources/example-response.json").mkString

        val extracted = loader.extractText(response)
        assert(extracted != null)
        assert(extracted.nonEmpty)

        val expectedText = scala.io.Source.fromFile("src/test/test-resources/example-text.txt").mkString
        assert(expectedText == extracted)
    }

    it should "extract wiki text from all pages" in {
        val response = "{ \"query\": { \"pages\": { \"1\": { \"revisions\": [ { \"*\": \"lorem \" } ] }, \"2\": { \"revisions\": [ { \"*\": \"ipsum\" } ] } } } }"

        val extracted = loader.extractText(response)
        assert(extracted != null)
        assert(extracted.nonEmpty)
        assert(extracted == "lorem ipsum")
    }

    it should "fail extract text from malformed JSONS" in {

        intercept[WordCountException] {
            loader.extractText("")
        }
        intercept[WordCountException] {
            loader.extractText("lorem ipsum")
        }
        intercept[WordCountException] {
            loader.extractText("{ \"lorem\": \"ipsum\" }")
        }
        intercept[WordCountException] {
            loader.extractText("{ \"query\": \"ipsum\" }")
        }
    }

    it should "fail when JSON contains error directive" in {
        intercept[WordCountException] {
            loader.extractText("{ \"error\": { \"info\": \"Error message\" } }")
        }
    }
}
