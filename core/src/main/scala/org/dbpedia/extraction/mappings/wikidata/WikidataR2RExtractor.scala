package org.dbpedia.extraction.mappings

import java.io._

import org.dbpedia.extraction.config.mappings.wikidata._
import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.ontology.{Ontology}
import org.dbpedia.extraction.util.{WikidataUtil, Language}
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

/**
 * Created by ali on 10/26/14.
 */
class WikidataR2RExtractor(
                               context : {
                                      def ontology : Ontology
                                      def language : Language
                                    }
                            )
  extends JsonNodeExtractor
{

  // this is where we will store the output
  val WikidataR2RDataSet = new Dataset("wikidata-r2r")
  override val datasets = Set(WikidataR2RDataSet)

  val config:WikidataExtractorConfig= WikidataExtractorConfigFactory.createConfig("config.json")

  override def extract(page : JsonNode, subjectUri : String, pageContext : PageContext): Seq[Quad] =
  {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()
    val receiver:WikidataCommandReceiver = new WikidataCommandReceiver

    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
      statementGroup.getStatements.foreach {
        statement => {
          val claim = statement.getClaim()
          val property = WikidataUtil.replacePropertyId(claim.getMainSnak().getPropertyId().toString).
            replace("http://data.dbpedia.org/resource/", "").trim

          claim.getMainSnak() match {
            case mainSnak: ValueSnak => {
              val value = mainSnak.getValue
              val command:WikidataTransformationCommands = config.getCommand(property,value, receiver)
              command.execute()
              quads ++=getQuad(page, subjectUri, receiver.getMap())
            }

            case _ =>
          }

        }
      }
    }
    quads
  }

  def getQuad(page : JsonNode, subjectUri : String, map:mutable.Map[String,String]): ArrayBuffer[Quad] = {
    val quads = new ArrayBuffer[Quad]()
    map.foreach {
      propertyValue => {
        try {
          val ontologyProperty = context.ontology.properties(propertyValue._1)
          val datatype = null
          quads +=new Quad(context.language, WikidataR2RDataSet, subjectUri, ontologyProperty, propertyValue._2, page.wikiPage.sourceUri,datatype)
        } catch {
          case e:Exception => println("exception caught: " + e)
        }

      }
    }
    quads
  }

}

