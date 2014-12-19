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
  private val rdfTypeProperty = context.ontology.properties("rdf:type")
  private val geoLatProperty = context.ontology.properties("geo:lat")
  private val geoLongProperty = context.ontology.properties("geo:long")
  private val geoRssProperty = context.ontology.properties("georss:point")
  private val featureOntClass = context.ontology.classes("geo:SpatialThing")

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

//      getDBpediaSameasProperties(property).foreach {
//        dbProp => {
//          if (property != "http://data.dbpedia.org/resource/P625") WikidataExtractorConfig.configFileExample+= "replace property " + property + " " + dbProp
//        }
//      }
      WikidataExtractorConfig.conf(property, "", "property") //set property


      claim.getMainSnak() match {
        case mainSnak: ValueSnak => {
          val value = mainSnak.getValue.toString.replace("(item)", "").replace("\"", "").trim
          WikidataExtractorConfig.conf(property, value, "value") //set value

          WikidataExtractorConfig.MapResult.get(property).foreach(map =>
          {
            if (!map.isEmpty) map.foreach {
              kv => {
                val mappedValue = kv._2 match {
                  case s => s
                  case _=> value
                }
                val mappedProperty = propertyStringMatch(kv._1)
                quads += new Quad(context.language, WikidataTestDataSet, subjectUri,mappedProperty.toString, mappedValue.toString, page.wikiPage.sourceUri, null)
              }
            }
          }
          )

//          p match {
//            case "owlSameAs" => quads += new Quad(context.language, WikidataTestDataSet, subjectUri, sameAsProperty, o, page.wikiPage.sourceUri, null)
//            case p if p.startsWith("http://dbpedia.org/ontology/") => quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, null)
//            case _ => quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, context.ontology.datatypes("xsd:string"))
//          }

        }
        case _ =>
      }

    }
    quads
  }

  def propertyStringMatch(newProperty: String = "") = newProperty match {
      case "owlSameAs" => sameAsProperty
      case "rdfType" => rdfTypeProperty
      case "geoLat" => geoLatProperty
      case "geoLong" => geoLongProperty
      case "geoRss" => geoRssProperty
      case _ => newProperty
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

