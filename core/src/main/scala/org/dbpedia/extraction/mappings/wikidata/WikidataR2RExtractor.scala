package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.config.mappings.{ReplaceFunction, WikidataExtractorConfig}
import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.mappings.{JsonNodeExtractor, PageContext}
import org.dbpedia.extraction.ontology.{OntologyProperty, Ontology}
import org.dbpedia.extraction.util.Language
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces._

import scala.collection.JavaConversions._
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
  // Here we define all the ontology predicates we will use
  private val sameAsProperty = context.ontology.properties("owl:sameAs")

  // this is where we will store the output
  val WikidataTestDataSet = new Dataset("wikidata-r2r")
  override val datasets = Set(WikidataTestDataSet)


  override def extract(page : JsonNode, subjectUri : String, pageContext : PageContext): Seq[Quad] =
  {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()
    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
      //println(statementGroup)
      val claim = statementGroup.getStatements().get(0).getClaim()
      val property = claim.getMainSnak().getPropertyId().toString().replace("(property)", "").trim

      getDBpediaSameasProperties(property).foreach {
        dbProp => WikidataExtractorConfig.configFileExample+= "replace property " + property + " " + dbProp
      }
      val p = WikidataExtractorConfig.conf(property, "", "property").getOrElse("property",property)


      claim.getMainSnak() match {
        case mainSnak: ValueSnak => {
              val value = mainSnak.getValue
              println(value.getClass)
              val newValue =value.toString.replace("(item)", "").replace("\"", "").trim
              val o = WikidataExtractorConfig.conf(property, newValue, "value").getOrElse("value", newValue)
              p match {
                  case "owlSameAs" => quads += new Quad(context.language, WikidataTestDataSet, subjectUri, sameAsProperty, o, page.wikiPage.sourceUri, null)
                  case p if p.startsWith("http://dbpedia.org/ontology/")=> quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, null)
                  case _=> quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, context.ontology.datatypes("xsd:string"))
              }

            }
        case _ =>
        }

    }
    quads
  }

  def getDBpediaSameasProperties(property:String) : Set[OntologyProperty] =
  {
    val p = property.replace("http://data.dbpedia.org/resource","http://wikidata.dbpedia.org/resource")
    var properties = Set[OntologyProperty]()
    context.ontology.equivalentPropertiesMap.foreach({map =>
      if (map._1.toString.matches(p)) {
        map._2.foreach { mappedProp =>
          properties += mappedProp
        }
      }
    })
    properties
  }
}

