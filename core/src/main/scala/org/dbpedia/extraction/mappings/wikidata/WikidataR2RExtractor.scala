package org.dbpedia.extraction.mappings

import java.io._

import org.dbpedia.extraction.config.mappings.wikidata._
import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.ontology.{Ontology}
import org.dbpedia.extraction.util.Language
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
  private val geoSpatialThing = context.ontology.classes("geo:SpatialThing")

  // this is where we will store the output
  val WikidataTestDataSet = new Dataset("wikidata-r2r")
  override val datasets = Set(WikidataTestDataSet)

  val config:WikidataExtractorConfig= WikidataExtractorConfigFactory.createConfig("config.json")

  override def extract(page : JsonNode, subjectUri : String, pageContext : PageContext): Seq[Quad] =
  {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()
    val receiver:WikidataCommandReceiver = new WikidataCommandReceiver


    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
      val claim = statementGroup.getStatements().get(0).getClaim()
      val property = claim.getMainSnak().getPropertyId().toString().replace("(property)", "").
        replace("http://data.dbpedia.org/resource/", "").trim
      claim.getMainSnak() match {
        case mainSnak: ValueSnak => {
          val value = mainSnak.getValue //.toString.replace("(item)", "").replace("\"", "").trim
          val command:WikidataTransformationCommands = config.getCommand(property,value, receiver)
          command.execute()
          quads ++=getQuad(page, subjectUri, receiver.getMap())
        }

        case _ =>
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
          if (propertyValue._2 == "$getSpatialThing")
            quads +=new Quad(context.language, WikidataTestDataSet, subjectUri, ontologyProperty, geoSpatialThing.toString, page.wikiPage.sourceUri,datatype)
          else
            quads +=new Quad(context.language, WikidataTestDataSet, subjectUri, ontologyProperty, propertyValue._2, page.wikiPage.sourceUri,datatype)
        } catch {
          case e:Exception => println("exception caught: " + e)
        }

      }
    }
    quads
  }

}

