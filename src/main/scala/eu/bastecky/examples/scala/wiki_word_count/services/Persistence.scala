package eu.bastecky.examples.scala.wiki_word_count.services

import java.io.{IOException, InputStream, StringWriter}
import java.sql.{Connection, DriverManager, SQLException, Statement}

import eu.bastecky.examples.scala.wiki_word_count.beans.{TextEntry, Word, WordCountException}
import org.apache.commons.io.IOUtils

import scala.collection.mutable

/**
  * Defines methods of persistence layer for caching processed TextEntries.
  */
trait Persistence {

    /**
      * Loads stored TextEntry with query value matching given parameter. None value is returned if TextEntry is missing
      * for given database.
      *
      * @param textSource Identification of text source
      * @param query Search query which uniquely identifies required TextEntry object within each text source
      * @return Stored TExtEntry object or None if there is no persisted value.
      */
    def getTextEntry(textSource: String, query: String): Option[TextEntry]

    /**
      * Store given text entry into persistence layer. Stored cache entry can be loaded back by its query parameter
      * using method getTextEntry.
      *
      * Important: TextEntry is uniquely identified by its query parameter. New entry with the same query will override
      * the previous one.
      *
      * @param entry TextEntry to be persisted in persistence layer.
      */
    def storeTextEntry(entry: TextEntry)
}

/**
  * Implementation of persistence based on runtime DERBY database.
  */
class DerbyPersistence()(implicit config: Configuration) extends Persistence {

    // Filename with definition of tables - will be executed in constructor of this class (in case that tables are missing)
    val createScriptFileName = "create-tables.sql"

    // Table names
    val textEntryTableName = "text_entries"
    val wordsTableName = "words"

    // Used fields in tables
    val textSourceField = "text_source"
    val queryField = "query"
    val textField = "text"
    val textEntryIdField = "text_entry_id"
    val wordField = "word"
    val wordCountField = "word_count"
    val idField = "id"

    // Used SQL queries
    val deleteTextEntrySQL = s"DELETE FROM $textEntryTableName WHERE $textSourceField = ? and $queryField = ?"
    val insertTextEntrySQL = s"INSERT INTO $textEntryTableName($textSourceField, $queryField, $textField) VALUES(?, ?, ?)"
    val selectTextEntrySQL = s"SELECT * FROM $textEntryTableName WHERE $textSourceField = ? and $queryField = ?"
    val insertWordSQL = s"INSERT INTO $wordsTableName($textEntryIdField, $wordField, $wordCountField) VALUES(?, ?, ?)"
    val selectWordsSQL = s"Select * FROM $wordsTableName WHERE $textEntryIdField = ? ORDER BY $idField"

    // Configuration
    val dbName = config.getValue(Configuration.DerbyDatabaseNameProperty)
    val isMemory = config.getBoolValue(Configuration.DerbyDatabaseIsMemoryProperty)

    // Check whether there are tables and creates them if not
    ensureTables()

    override def getTextEntry(textSource: String, query: String): Option[TextEntry] = execute[Option[TextEntry]]((conn: Connection) => {

        // Select text entry matching given text source and query
        val selectTextEntryStmt = conn.prepareStatement(selectTextEntrySQL)
        selectTextEntryStmt.setString(1, textSource)
        selectTextEntryStmt.setString(2, query)
        val textEntryRS = selectTextEntryStmt.executeQuery()

        if (textEntryRS.next()) {
            // We have some text entry with given parameters - load words

            // Select words with text entry ID of resolved text entry
            val selectWordsStmt = conn.prepareStatement(selectWordsSQL)
            selectWordsStmt.setLong(1, textEntryRS.getLong(idField))
            val wordsRS = selectWordsStmt.executeQuery()

            // Create collection of words
            val words = new mutable.ArrayBuffer[Word](wordsRS.getFetchSize)
            while (wordsRS.next()) {
                words += Word(
                    wordsRS.getString(wordField),
                    wordsRS.getInt(wordCountField)
                )
            }

            // Create and return text entry object
            Some(TextEntry(
                textEntryRS.getString(textSourceField),
                textEntryRS.getString(queryField),
                textEntryRS.getString(textField),
                words
            ))
        }
        //Else we have no text entry with specified parameters - return None value
        else None
    })

    override def storeTextEntry(entry: TextEntry): Unit = execute((conn: Connection) => {
        try {
            // Drop existing text entry object with the same text source and query - will drop words in cascade
            // Do nothing when there is no text entry in the table
            val deleteTextEntryStmt = conn.prepareStatement(deleteTextEntrySQL)
            deleteTextEntryStmt.setString(1, entry.textSource)
            deleteTextEntryStmt.setString(2, entry.query)
            deleteTextEntryStmt.execute()

            // Insert new text entry
            val insertTextEntryStmt = conn.prepareStatement(insertTextEntrySQL, Statement.RETURN_GENERATED_KEYS)
            insertTextEntryStmt.setString(1, entry.textSource)
            insertTextEntryStmt.setString(2, entry.query)
            insertTextEntryStmt.setString(3, entry.text)
            insertTextEntryStmt.execute()

            // Resolve ID of newly inserted text entry
            val textEntryIdRs = insertTextEntryStmt.getGeneratedKeys
            if (textEntryIdRs.next()) {
                val textEntryId = textEntryIdRs.getLong(1)

                // Prepare statement for inserting all words at once
                val wordStmt = conn.prepareStatement(insertWordSQL)
                entry.words.foreach(word => {
                    wordStmt.setLong(1, textEntryId)
                    wordStmt.setString(2, word.word)
                    wordStmt.setInt(3, word.count)
                    wordStmt.addBatch()
                })

                // Insert words into table
                wordStmt.executeBatch()
            }
            else throw new WordCountException("Persistence error - Cannot insert text entry into database: Unable to resolve ID of newly inserted text entry")
        }
        catch {
            case e: SQLException => throw new WordCountException(s"Persistence error - cannot insert text entry into database. SQL error: ${e.getMessage}")
        }
    })

    /**
      * Ensure existence of used tables by running create script.
      */
    private def ensureTables(): Unit = execute((conn: Connection) => {
        // Check existence of tables
        val metadata = conn.getMetaData
        val rsMetadata = metadata.getTables(null, null, textEntryTableName.toUpperCase(), null)

        // Tables doesn't exist - create them using create script
        if (!rsMetadata.next()) executeScript(createScriptFileName, conn)
    })

    /**
      * Prepares connection to database with autocommit set to false. Any update has to be committed manually.
      */
    def createConnection() = {
        // Resolve connection string to database specified in configuration
        val dbUrl = if (isMemory) s"jdbc:derby:memory:$dbName;create=true"
        else s"jdbc:derby:$dbName;create=true"

        // Create connection object
        val connection = DriverManager.getConnection(dbUrl)
        connection.setAutoCommit(false)

        connection
    }

    /**
      * Executes given sql operation on database. Handles lifecycle of connection object - creation, commit, rollback,
      * close. Connection is committed when operation is completed or rolled back when operation fails by throwing
      * any exception.
      *
      * @param sqlOperation Method with operation to be executed. Receives connection object as parameter.
      */
    def execute(sqlOperation: (Connection) => Unit) = execute[Unit](sqlOperation)

    /**
      * Executes given sql operation on database and returns result of operation. Handles lifecycle of connection object
      * - creation, commit, rollback, close. Connection is committed when operation is completed or rolled back when
      * operation fails by throwing any exception.
      *
      * @param sqlOperation Method with operation to be executed. Receives connection object as parameter.
      * @tparam T Type of returned value of operation
      * @return Result of sql operation
      */
    def execute[T](sqlOperation: (Connection) => T) = {
        var conn: Option[Connection] = None
        var isCommitted = false
        try {
            // Create connection object
            conn = Some(createConnection())

            // Execute operation with created connection
            val result = sqlOperation(conn.get)

            // Commit changes
            conn.get.commit()
            isCommitted = true

            // Return result of operation
            result
        }
        finally {
            if (conn.isDefined)  {
                // Something went wrong - perform rollback
                if (!isCommitted) conn.get.rollback()

                // Don't forget to close connection
                conn.get.close()
            }
        }
    }

    /**
      * Loads and executes SQL script file located at classpath.
      */
    def executeScript(scriptName: String, conn: Connection) = {
        var input: Option[InputStream] = None
        try {
            // Load script content from classpath
            input = Some(getClass.getClassLoader.getResourceAsStream(createScriptFileName))
            val script = IOUtils.toString(input.get)

            // Execute each statement in script
            val stmt = conn.createStatement()
            script.split(";").foreach(stmt.executeUpdate)
        }
        catch {
            case e: IOException =>
                throw new WordCountException(s"Persistence error - cannot get SQL script '$scriptName' from classpath: ${e.getMessage}")

            case e: SQLException =>
                throw new WordCountException(s"Persistence error - cannot execute script '$scriptName': ${e.getMessage}")
        }
        finally {
            // Don't forget to close input stream
            if (input.isDefined) input.get.close()
        }
    }
}

