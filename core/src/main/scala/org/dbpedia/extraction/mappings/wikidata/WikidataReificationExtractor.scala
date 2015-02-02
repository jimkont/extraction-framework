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
//  private val rdfStatement = context.ontology.properties("rdf:Statement")
//  private val rdfSubject = context.ontology.properties("rdf:subject")
//  private val rdfPredicate= context.ontology.properties("rdf:predicate")
//  private val rdfObject = context.ontology.properties("rdf:object")

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
                    val statementUri= getStatementUri(subjectUri, property, getItemId(value))
                    println(statementUri)
                    val qualifierMap = splitQualifier(qualifier.toString)
                    val qualifierProperty = qualifierMap.keys.head
                    val qualifierValue = qualifierMap.get(qualifierProperty)
//                    quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfType, rdfStatement.uri, page.wikiPage.sourceUri)
//                    quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfSubject, subjectUri, page.wikiPage.sourceUri,null)
//                    quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfPredicate, property, page.wikiPage.sourceUri,null)
//                    quads += new Quad(context.language, WikidataReificationDataSet, statementUri, rdfObject, value.toString, page.wikiPage.sourceUri,null)
//                    println(subjectUri + " " + property + " " + value + " " )
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

  def getItemId(value:Value) = value match {
      case v:ItemIdValue => WikidataUtil.replaceItemId(v.toString)
      case _ => getHashId()
  }

  def getStatementUri(subject:String, property:String,itemId:String):String = {
    subject+"_"+ property.replace("http://data.dbpedia.org/resource/", "").trim+"_" + itemId.replace("http://data.dbpedia.org/resource/","")
  }

  def splitQualifier(qualifier:String):Map[String,String] = {
    Map(qualifier.toString.split("::")(0) ->  qualifier.toString.split("::")(1))
  }

  def getHashId():String={
   "testString"
  }

}

