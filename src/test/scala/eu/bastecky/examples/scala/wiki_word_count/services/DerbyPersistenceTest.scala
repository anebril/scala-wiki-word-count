package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.TestConfiguration
import eu.bastecky.examples.scala.wiki_word_count.beans.{TextEntry, Word}
import org.scalatest.FlatSpec

/**
  * Created by pavel on 6/12/16.
  */
class DerbyPersistenceTest extends FlatSpec {

    behavior of "DerbyPersistenceTest"

    it should "test check creation of database tables" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-01"
        ))

        val persistence = new DerbyPersistence()(config)
        val conn = persistence.createConnection()
        conn.setAutoCommit(true)

        val metadata = conn.getMetaData

        // Check text entry table
        val textEntryRS = metadata.getTables(null, null, persistence.textEntryTableName.toUpperCase(), null)
        assert(textEntryRS.next())

        // Check words table
        val wordsRS = metadata.getTables(null, null, persistence.wordsTableName.toUpperCase(), null)
        assert(wordsRS.next())

        conn.close()
    }

    it should "persist new single text entry and read it back" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-02"
        ))

        val textEntry = TextEntry("text-source", "query", "Lorem ipsum dolor sit amet.",
            Seq(Word("lorem", 5), Word("ipsum", 2), Word("dolor", 1))
        )

        val persistence = new DerbyPersistence()(config)
        persistence.storeTextEntry(textEntry)

        val conn = persistence.createConnection()
        conn.setAutoCommit(true)

        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM words JOIN text_entries ON text_entries.id = words.text_entry_id order by words.id")

        var i = 0
        while (rs.next()) {
            assert(i < textEntry.words.size)

            assert(rs.getString(persistence.textSourceField) == textEntry.textSource)
            assert(rs.getString(persistence.queryField) == textEntry.query)
            assert(rs.getString(persistence.textField) == textEntry.text)

            assert(rs.getString(persistence.wordField) == textEntry.words(i).word)
            assert(rs.getInt(persistence.wordCountField) == textEntry.words(i).count)
            i += 1
        }

        assert(i == textEntry.words.size)
    }

    it should "persist and override single text entry and read it back" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-03"
        ))

        val persistence = new DerbyPersistence()(config)

        val textEntryToBeOverridden = TextEntry("text-source", "query", "Some text which will be overriden.",
            Seq(Word("lorem", 6), Word("text", 2))
        )
        persistence.storeTextEntry(textEntryToBeOverridden)

        val textEntry = TextEntry("text-source", "query", "Lorem ipsum dolor sit amet.",
            Seq(Word("lorem", 5), Word("ipsum", 2), Word("dolor", 1))
        )
        persistence.storeTextEntry(textEntry)

        val conn = persistence.createConnection()
        conn.setAutoCommit(true)

        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM words JOIN text_entries ON text_entries.id = words.text_entry_id order by words.id")

        var i = 0
        while (rs.next()) {
            assert(i < textEntry.words.size)

            assert(rs.getString(persistence.textSourceField) == textEntry.textSource)
            assert(rs.getString(persistence.queryField) == textEntry.query)
            assert(rs.getString(persistence.textField) == textEntry.text)

            assert(rs.getString(persistence.wordField) == textEntry.words(i).word)
            assert(rs.getInt(persistence.wordCountField) == textEntry.words(i).count)
            i += 1
        }

        assert(i == textEntry.words.size)
    }

    it should "persist multiple distinct text entries and read it back" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-04"
        ))

        val persistence = new DerbyPersistence()(config)

        val textEntries = Seq(
            TextEntry("text-source", "query", "Lorem ipsum dolor sit amet.",
                Seq(Word("lorem", 6), Word("ipsum", 2), Word("dolor", 2))),
            TextEntry("text-source", "another query", "Nechť již hříšné saxofony ďáblů rozzvučí síň úděsnými tóny waltzu, tanga a quickstepu.",
                Seq(Word("nechť", 6), Word("hříšné", 2), Word("ďáblů", 2), Word("úděsnými", 1))),
            TextEntry("text-source", "empty query", "", Seq()),
            TextEntry("other-text-source", "query", "Some text splitted \n into \n several lines",
                Seq(Word("lorem", 6), Word("ipsum", 2), Word("dolor", 2)))
        )

        textEntries.foreach(persistence.storeTextEntry)

        val conn = persistence.createConnection()
        conn.setAutoCommit(true)

        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT * FROM words RIGHT JOIN text_entries ON text_entries.id = words.text_entry_id order by text_entries.id, words.id")

        var t = 0
        var w = 0
        while (rs.next()) {
            val textEntry = textEntries(t)
            assert(rs.getString(persistence.textSourceField) == textEntry.textSource)
            assert(rs.getString(persistence.queryField) == textEntry.query)
            assert(rs.getString(persistence.textField) == textEntry.text)

            if (textEntry.words.size > w) {
                assert(rs.getString(persistence.wordField) == textEntry.words(w).word)
                assert(rs.getInt(persistence.wordCountField) == textEntry.words(w).count)
            }
            w += 1

            if (textEntry.words.size <= w) {
                w = 0
                t += 1
            }
        }
    }

    it should "read persisted text entry from DB" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-05"
        ))

        val textEntry = TextEntry("text-source", "query", "Lorem ipsum dolor sit amet.",
            Seq(Word("lorem", 5), Word("ipsum", 2), Word("dolor", 1))
        )

        val persistence = new DerbyPersistence()(config)

        val conn = persistence.createConnection()
        conn.setAutoCommit(true)
        val stmt = conn.createStatement()
        stmt.executeUpdate(s"insert into ${persistence.textEntryTableName}(${persistence.textSourceField}, ${persistence.queryField}, ${persistence.textField}) " +
          s"values('${textEntry.textSource}', '${textEntry.query}', '${textEntry.text}')")

        textEntry.words.foreach(word =>
            stmt.executeUpdate(s"insert into ${persistence.wordsTableName}(${persistence.textEntryIdField}," +
              s"${persistence.wordField}, ${persistence.wordCountField}) values(1, '${word.word}', ${word.count})")
        )

        val resolved = persistence.getTextEntry(textEntry.textSource, textEntry.query)
        assert(resolved != null)
        assert(resolved.isDefined)
        assert(resolved.get == textEntry)
    }

    it should "not read missing text entry from DB" in {
        val config = new TestConfiguration(Map(
            Configuration.DerbyDatabaseIsMemoryProperty -> "true",
            Configuration.DerbyDatabaseNameProperty -> "test-db-06"
        ))

        val persistence = new DerbyPersistence()(config)
        val resolved = persistence.getTextEntry("text-source", "query")
        assert(resolved != null)
        assert(resolved.isEmpty)
    }
}
