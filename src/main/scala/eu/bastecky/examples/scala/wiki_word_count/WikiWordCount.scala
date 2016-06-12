package eu.bastecky.examples.scala.wiki_word_count

import eu.bastecky.examples.scala.wiki_word_count.beans.WordCountException
import eu.bastecky.examples.scala.wiki_word_count.services._

/**
  * Main class of wiki word count applications. This program loads article from wikipedia and print out counted words.
  */
object WikiWordCount extends App {

    // Prepare instances of used services (uses implicit mechanism to perform dependency injection)
    implicit lazy val config = new PropertyConfiguration
    implicit lazy val persistence: Persistence = new DerbyPersistence
    implicit lazy val textLoader: TextLoader = new WikiTextLoader
    implicit lazy val wordCounter: WordCounter = new WikiWordCounter
    implicit lazy val textEntryService = new TextEntryService()

    if (args.isEmpty) {
        // Write usage to standard output if there is no argument
        println("Usage: java -jar wiki-word-count.jar \"search query\"")
        System.exit(-1)
    }
    else try {
        // Use first argument as query
        val query = args(0)

        // Obtain text entry matching specified query
        val textEntry = textEntryService.getEntry(query)

        // Create report and print it to standard output
        val report = textEntryService.createTextEntryReport(textEntry)
        println(report)
    }
    catch {
        // An error occurred report it to user
        case e: WordCountException => println(s"Cannot perform requested operation. ${e.getMessage}")
        case e: Throwable => println(s"Cannot perform requested due to unhandled internal error. " +
          s"Error type: ${e.getClass.getName}, Error: ${e.getMessage}")
    }
}
