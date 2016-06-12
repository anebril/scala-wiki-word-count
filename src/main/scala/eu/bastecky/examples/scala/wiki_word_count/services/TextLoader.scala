package eu.bastecky.examples.scala.wiki_word_count.services

import java.net.URLEncoder

import eu.bastecky.examples.scala.wiki_word_count.beans.WordCountException

import scala.io.Source
import scala.util.parsing.json.JSON

/**
  * Define methods for loading texts from text source. Text source can be either local (database) or remote (web page).
  */
trait TextLoader {

    /**
      * Loads text from a text source for given search query parameter. Method will send search query to a text source
      * and obtains matching text.
      *
      * Method can throw an exception if text source couldn't be connected.
      *
      * @param query Search query to be resolved as requested text
      * @return REquested text matching search query
      */
    def load(query: String): String
}

/**
  * Implementation of text loader which loads content of wikipedia article matching given search parameter. Loader uses
  * public wikipedia API to load articles. Loader performs HTTP request to wikipedia API and receives JSON response.
  *
  * More info about wiki api can be found at: https://www.mediawiki.org/wiki/API:Main_page
  */
class WikiTextLoader()(implicit val config: Configuration) extends TextLoader {

    val wikiEndpoint = config.getValue(Configuration.WikiEndpointProperty)
    val wikiQueryParam = config.getValue(Configuration.WikiQueryParamProperty)

    class Extractor[T] { def unapply(a:Any):Option[T] = Some(a.asInstanceOf[T]) }
    object MapExtractor extends Extractor[Map[String, Any]]

    override def load(query: String): String = {
        val content = performRequest(query, wikiEndpoint, wikiQueryParam)
        extractText(content)
    }

    /**
      * Sends search request to wikipedia API and returns retrieved string. Wikipedia URI and query parameters are set
      * in configuration - WikiEndpointProperty and WikiQueryParamProperty.
      *
      * @param query Search query to be resolved as requested text
      * @return Received text response to sent request
      */
    def performRequest(query: String, wikiEndpoint: String, wikiQueryParam: String): String = {

        // Prepare uri with encoded search query
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val uri = s"$wikiEndpoint&$wikiQueryParam=$encodedQuery"

        try {
            // Load content from remote server
            val html = Source.fromURL(uri)
            html.mkString
        }
        catch {
            case e: Exception =>
                throw new WordCountException(s"Error loading text from wiki page - Error connecting remote server: ${e.getMessage}")
        }
    }

    /**
      * Parses wiki response and extracts text of received wiki article.
      *
      * @param content Text with received JSON response
      * @return Extracted article text
      * @throws WordCountException Thrown on any error in received JSON - invalid format, error response, missing
      *                            required parts of response
      */
    def extractText(content: String): String =
        JSON.parseFull(content) match {

            // Given string is valid JSON
            case Some(json) =>
                try {

                    // Get root map of entries and navigate to query.pages
                    val map = json.asInstanceOf[Map[String, Any]]
                    if (!map.contains("error")) {
                        val queryMap = getValue(map, "query").asInstanceOf[Map[String, Any]]
                        val pagesMap = getValue(queryMap, "pages").asInstanceOf[Map[String, Any]]

                        // Resolves text of all pages x.revisions[1].*
                        val texts = pagesMap.map((elem: (String, Any)) => {
                            val pageMap = elem._2.asInstanceOf[Map[String, Any]]
                            val revisionList = getValue(pageMap, "revisions").asInstanceOf[List[Any]]
                            val headRevision = revisionList.head.asInstanceOf[Map[String, Any]]
                            val text = getValue(headRevision, "*").asInstanceOf[String]

                            text
                        })

                        // Concat text of all pages together
                        val sb = new StringBuilder
                        texts.foreach(s => sb ++= s)
                        sb.toString()
                    }
                    else {
                        // Received error response from api (wrong request) - report it to user
                        val MapExtractor(errorMap) = getValue(map, "error")
                        val msg = getValue(errorMap, "info").asInstanceOf[String]
                        throw new WordCountException(s"Error loading text from wiki page - Received error response: $msg")
                    }
                }
                catch {
                    case e: ClassCastException =>
                        throw new WordCountException("Error loading text from wiki page - Invalid structure of recieved JSON response")
                }

            // Given string is not JSON - throw exception with error
            case None =>
                throw new WordCountException("Error loading text from wiki page - Cannot parse JSON value from received answer")
        }


    /**
      * Helper method to get key from a map. Throws an exception with error when key is not present.
      */
    def getValue(map: Map[String, Any], key: String) =
        if (map.contains(key)) map.get(key).get
        else throw new WordCountException(s"Error loading text from wiki page: Missing required key '$key' in received JSON answer")

}