package eu.bastecky.examples.scala.wiki_word_count

import eu.bastecky.examples.scala.wiki_word_count.beans.WordCountException
import eu.bastecky.examples.scala.wiki_word_count.services._
import org.slf4j.LoggerFactory

/**
  * Main class of wiki word count applications. This program loads article from wikipedia and print out counted words.
  */
object WikiWordCount extends App {

    val logger = LoggerFactory.getLogger(WikiWordCount.getClass)

    logger debug s"Starting wiki word count application"

    // Prepare instances of used services (uses implicit mechanism to perform dependency injection)
    logger trace s"Preparing service instances"
    implicit lazy val config = new PropertyConfiguration
    implicit lazy val persistence: Persistence = new DerbyPersistence
    implicit lazy val textLoader: TextLoader = new WikiTextLoader
    implicit lazy val wordCounter: WordCounter = new WikiWordCounter
    implicit lazy val textEntryService = new TextEntryService()
    logger trace s"Service instances was successfully created"

    if (args.isEmpty) {
        logger error s"Search query was not specified as argument - exiting now"

        // Write usage to standard output if there is no argument
        println("Usage: java -jar wiki-word-count.jar \"search query\"")
        System.exit(-1)
    }
    else try {
        // Use first argument as query
        val query = args(0).toLowerCase
        logger debug s"Resolving text entry matching specified query: $query"

        // Obtain text entry matching specified query
        logger trace s"Resolving text entry"
        val textEntry = textEntryService.getEntry(query)

        // Create report and print it to standard output
        logger trace s"Making report"
        val report = textEntryService.createTextEntryReport(textEntry)

        logger trace s"Printing report to standard output"
        println(report)

        logger debug s"Program finished successfully. Exiting now."
    }
    catch {
        // An error occurred report it to user
        case e: WordCountException =>
            println(s"Cannot perform requested operation. ${e.getMessage}")
            logger.error("Word counter error ", e)

        case e: Throwable =>
            println(s"Cannot perform requested due to unhandled internal error. Error type: ${e.getClass.getName}, Error: ${e.getMessage}")
            logger.error("Unhandled internal error ", e)
    }
}
