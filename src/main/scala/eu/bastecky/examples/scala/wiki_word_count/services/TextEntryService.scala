package eu.bastecky.examples.scala.wiki_word_count.services

import eu.bastecky.examples.scala.wiki_word_count.beans.{TextEntry, WordCountException}

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

    val textSource = config.getValue(Configuration.TextSourceProperty)

    /**
      * Resolves text entry for given query parameter. Returns persisted text entry if there is any matching given query
      * or creates new request to text source.
      *
      * @param query Search query for text source
      * @return Resolved text entry for given search query
      */
    def getEntry(query: String): TextEntry = {

        // First check if there is some cached entry for given query
        val cached = persistence.getTextEntry(textSource, query)

        if (cached.isEmpty) {
            // There is no persisted query - load text using text loader and count words
            val text = textLoader.load(query)
            val words = wordCounter.countWords(text)

            TextEntry(textSource, query, text, words)
        }
        else {
            // Return cached value
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
            persistence.storeTextEntry(textEntry)
        }
        catch {
            case e: WordCountException => println("Cannot cache (todo logger): " + e.getMessage)
        }
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
