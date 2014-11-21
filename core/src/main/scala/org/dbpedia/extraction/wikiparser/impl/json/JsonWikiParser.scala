package org.dbpedia.extraction.wikiparser.impl.json

import org.dbpedia.extraction.sources.WikiPage
import org.dbpedia.extraction.wikiparser.{Node, PageNode, WikiTitle,JsonNode}
import com.fasterxml.jackson.databind.ObjectMapper
import org.wikidata.wdtk.datamodel.json.jackson.JacksonItemDocument
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.util.matching.Regex

import JsonWikiParser._


/**
 * Created with IntelliJ IDEA.
 * User: andread
 * Date: 08/04/13
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
object JsonWikiParser {
  /* the regex should search for languageslinks like "enwiki" only
  so that "enwikivoyage" for example wouldn't be acceptable because they wouldn't match a DBpedia entity
  */
  private val WikiLanguageRegex = """([^\s]+)wiki$"""
}

/**
 * JsonWikiParser class use wikidata Toolkit to parse wikidata json
 * wikidata json parsed and converted to wikidata ItemDocument
 */

class JsonWikiParser {

  def apply(page : WikiPage) : Option[JsonNode] =
  {
    if (page.format == null || page.format != "application/json")
    {
      None
    }
    else
    {

      val mapper = new ObjectMapper()
      val jsonString : java.lang.String = page.source
      val itemDocument : JacksonItemDocument = mapper.readValue(jsonString, classOf[JacksonItemDocument])
      //val jsonConverter = new JsonConverter("http://data.dbpedia.org/resource", new DataObjectFactoryImpl())
      //val Some(title) = IntRegEx findFirstIn page.title.toString()
      //val itemDocument : ItemDocument = jsonConverter.convertToItemDocument(jsonObject, "Q"+title)
      Some(new JsonNode(page,itemDocument))
    }
  }

}
  