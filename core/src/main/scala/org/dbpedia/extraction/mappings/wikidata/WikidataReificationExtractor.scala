package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.util.{Language, WikidataUtil}
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces._

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

/**
 * Created by ali on 10/26/14.
 */
class WikidataReificationExtractor(
                                    context: {
                                      def ontology: Ontology
                                      def language: Language
                                    }
                                    )
  extends JsonNodeExtractor {

  private val rdfType = context.ontology.properties("rdf:type")
  private val rdfStatement = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"
  private val rdfSubject = "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"
  private val rdfPredicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"
  private val rdfObject = "http://www.w3.org/1999/02/22-rdf-syntax-ns#object"

  // this is where we will store the output
  val WikidataReificationDataSet = new Dataset("wikidata-reification")
  override val datasets = Set(WikidataReificationDataSet)

  override def extract(page: JsonNode, subjectUri: String, pageContext: PageContext): Seq[Quad] = {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()

    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
      statementGroup.getStatements.foreach {
        statement => {
          val claim = statement.getClaim()
          val property = WikidataUtil.replacePropertyId(claim.getMainSnak().getPropertyId().toString)
          claim.getQualifiers.foreach {
            qualifier => {
              claim.getMainSnak() match {
                case mainSnak: ValueSnak => {
                  val value = mainSnak.getValue
                  val statementUri = WikidataUtil.getStatementUri(subjectUri, property, value)
                  val qualifierMap = WikidataUtil.splitQualifier(qualifier.toString)
                  val qualifierProperty = qualifierMap.keys.head
                  val qualifierValue = qualifierMap.get(qualifierProperty)
                  quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfType, rdfStatement, page.wikiPage.sourceUri)
                  quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfSubject, subjectUri, page.wikiPage.sourceUri, null)
                  quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfPredicate, property, page.wikiPage.sourceUri, null)
                  val datatype= getDatatype(value)
                  quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfObject, getValue(value), page.wikiPage.sourceUri, datatype)
                  qualifierValue match {
                    case Some(v) => quads += new Quad(context.language, WikidataReificationDataSet,
                        statementUri, qualifierProperty, v, page.wikiPage.sourceUri, context.ontology.datatypes("xsd:string"))

                  }
                }
                case _ =>
              }

            }
          }
        }
      }
    }
    quads
  }

  def getDatatype(value: Value) = value match {
    case value: ItemIdValue => {
      null
    }
    case value: StringValue => {
      context.ontology.datatypes("xsd:string")
    }

    case value: TimeValue => {
      context.ontology.datatypes("xsd:date")
    }
    case value: GlobeCoordinatesValue => {
      context.ontology.datatypes("xsd:string")
    }

    case value: QuantityValue => {
      context.ontology.datatypes("xsd:float")
    }
  }

  def getValue(value: Value) = value match {
    case value: ItemIdValue => {
      val v = WikidataUtil.replaceItemId(value.toString)
      v
    }
    case value: StringValue => {
      val v = WikidataUtil.replaceString(value.toString)
      v
    }

    case value: TimeValue => {
      val v = value.getYear + "-" + value.getMonth + "-" + value.getDay
      v
    }
    case value: GlobeCoordinatesValue => {
      val v = value.getLatitude + " " + value.getLongitude
      v
    }

    case value: QuantityValue => {
      val v = value.getNumericValue.toString
      v
    }
  }
}

