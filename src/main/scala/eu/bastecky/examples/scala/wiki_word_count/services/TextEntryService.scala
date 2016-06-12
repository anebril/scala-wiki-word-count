package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.{TextEntry, WordCountException}
import org.slf4j.LoggerFactory

/**
  * This class contains utility methods to work with text entries.
  */
class TextEntryService()(
                        implicit
                        config: Configuration,
                        persistence: Persistence,
                        textLoader: TextLoader,
                        wordCounter: WordCounter
                      ) {

    val logger = LoggerFactory.getLogger(classOf[TextEntryService])

    val textSource = config.getValue(Configuration.TextSourceProperty)

    /**
      * Resolves text entry for given query parameter. Returns persisted text entry if there is any matching given query
      * or creates new request to text source.
      *
      * @param query Search query for text source
      * @return Resolved text entry for given search query
      */
    def getEntry(query: String): TextEntry = {

        logger info s"Resolving text, textSource=$textSource, query=$query"

        // First check if there is some cached entry for given query
        logger trace "Loading text from cache"
        val cached = persistence.getTextEntry(textSource, query)

        if (cached.isEmpty) {
            logger debug "There is no cached text entry - loading from text source"

            // There is no persisted query - load text using text loader and count words
            logger trace "Loading from text words"
            val text = textLoader.load(query)

            logger trace "Analyzing word counts"
            val words = wordCounter.countWords(text)
            logger info s"Identified ${words.size} words in analyzed text"

            // Cache text entry and return it
            logger trace "Caching results"
            cacheTextEntry(TextEntry(textSource, query, text, words))
        }
        else {
            // Return cached value
            logger info "Returning cached text entry"
            cached.get
        }
    }

    /**
      * Cache given text entry using persistence layer. Cached text entries will be used when some query will occur again.
      *
      * @param textEntry Text entry to be persisted
      */
    def cacheTextEntry(textEntry: TextEntry) = {
        try {
            logger debug s"Caching loaded text entry: textSource=$textSource, query=${textEntry.query}"
            persistence.storeTextEntry(textEntry)
        }
        catch {
            case e: WordCountException =>
                logger error s"Result cannot be cached: ${e.getMessage}"
        }

        textEntry
    }

    /**
      * Creates text report describing given text entry. Report contains text source, query, original text and list of
      * words with numbers of occurrences.
      *
      * Report is formatted to be printed to user.
      *
      * @param textEntry Text entry to be processed
      * @return Formated report
      */
    def createTextEntryReport(textEntry: TextEntry): String = {

        // Calculate the largest size of word - used to align counts to column
        val maxWordSize = textEntry.words.foldLeft(0)((size, word) => Math.max(size, word.word.length))

        val sb = new StringBuilder
        sb.append(s"-- Text source: ------------------------------------------\n")
        sb.append(s"${textEntry.textSource}\n")
        sb.append(s"----------------------------------------------------------\n")
        sb.append(s"\n")
        sb.append(s"-- Search query: -----------------------------------------\n")
        sb.append(s"${textEntry.query}\n")
        sb.append(s"----------------------------------------------------------\n")
        sb.append(s"\n")
        sb.append(s"-- Returned text: ----------------------------------------\n")
        sb.append(textEntry.text)
        sb.append(s"\n")
        sb.append(s"----------------------------------------------------------\n")
        sb.append(s"\n")
        sb.append(s"-- Words: ------------------------------------------------\n")

        textEntry.words.foreach(w => {
            sb.append(s"${w.word}")
            val charactersToMax = maxWordSize - w.word.length
            if (charactersToMax > 0) {
                for (i <- 1 to charactersToMax) sb.append(" ")
            }
            sb.append(s" ${w.count}\n")
        })

        sb.append(s"----------------------------------------------------------\n")

        sb.toString
    }
}
