package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.TestConfiguration
import eu.bastecky.examples.scala.wiki_word_count.beans.{TextEntry, Word, WordCountException}
import org.scalatest.FlatSpec

/**
  * Created by pavel on 6/12/16.
  */
class TextEntryServiceTest extends FlatSpec {

    behavior of "TextEntryServiceTest"

    val config = new TestConfiguration(Map(Configuration.TextSourceProperty -> "test-source"))

    val persistedTextEntry = TextEntry("test-source", "query", "This is from persistence",
        Seq(Word("lorem", 1), Word("Ipsum", 1)))

    val loadedTextEntry = TextEntry("test-source", "query", "This is from textLoader.",
        Seq(Word("this", 1), Word("is", 1), Word("from", 1), Word("textloader", 1)))

    object TestPersistence$NoEntry extends Persistence {
        override def getTextEntry(textSource: String, query: String): Option[TextEntry] = None
        override def storeTextEntry(entry: TextEntry): Unit = ???
    }

    object TestPersistence$PersistedEntry extends Persistence {
        override def getTextEntry(textSource: String, query: String): Option[TextEntry] = Some(persistedTextEntry)
        override def storeTextEntry(entry: TextEntry): Unit = ???
    }

    object TestPersistence$Caching extends Persistence {
        var cached: Option[TextEntry] = None
        override def getTextEntry(textSource: String, query: String): Option[TextEntry] = ???
        override def storeTextEntry(entry: TextEntry): Unit = cached = Some(entry)
    }

    object TestPersistence$FailCaching extends Persistence {
        override def getTextEntry(textSource: String, query: String): Option[TextEntry] = ???
        override def storeTextEntry(entry: TextEntry): Unit = throw new WordCountException("Test")
    }

    object TestTextLoader extends TextLoader {
        override def load(query: String): String = loadedTextEntry.text
    }

    object TestWordCounter extends WordCounter {
        override def countWords(text: String): Seq[Word] = loadedTextEntry.words
    }

    it should "get text entry from persistence" in {
        val service = new TextEntryService()(config, TestPersistence$PersistedEntry, TestTextLoader, TestWordCounter)

        val entry = service.getEntry("query")
        assert(entry != null)
        assert(entry == persistedTextEntry)
    }

    it should "get text entry from loader" in {
        val service = new TextEntryService()(config, TestPersistence$NoEntry, TestTextLoader, TestWordCounter)

        val entry = service.getEntry("query")
        assert(entry != null)
        assert(entry == loadedTextEntry)
    }

    it should "cache text entry" in {
        val service = new TextEntryService()(config, TestPersistence$Caching, TestTextLoader, TestWordCounter)
        service.cacheTextEntry(loadedTextEntry)

        assert(TestPersistence$Caching.cached != null)
        assert(TestPersistence$Caching.cached.isDefined)
        assert(TestPersistence$Caching.cached.get == loadedTextEntry)
    }

    it should "fail to cache text entry" in {
        val service = new TextEntryService()(config, TestPersistence$FailCaching, TestTextLoader, TestWordCounter)

        service.cacheTextEntry(loadedTextEntry)
    }

    it should "create report of text entry" in {
        val service = new TextEntryService()(config, TestPersistence$PersistedEntry, TestTextLoader, TestWordCounter)

        val report = service.createTextEntryReport(loadedTextEntry)
        assert(report != null)
        assert(report.nonEmpty)
    }

    it should "create report of empty text entry" in {
        val service = new TextEntryService()(config, TestPersistence$PersistedEntry, TestTextLoader, TestWordCounter)

        val report = service.createTextEntryReport(TextEntry("test-source", "empty query", "", Seq.empty[Word]))
        println(report)
        assert(report.nonEmpty)
    }
}
