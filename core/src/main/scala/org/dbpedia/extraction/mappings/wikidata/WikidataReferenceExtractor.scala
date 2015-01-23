package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.destinations.{Dataset, DBpediaDatasets, Quad}
import org.dbpedia.extraction.mappings.{JsonNodeExtractor, PageContext}
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.util.Language
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

class WikidataReferenceExtractor(
                         context : {
                           def ontology : Ontology
                           def language : Language
                         }
                         )
  extends JsonNodeExtractor {

  val WikidataReferenceDataSet = new Dataset("wikidata-reference")
  override val datasets = Set(WikidataReferenceDataSet)

  override def extract(page: JsonNode, subjectUri: String, pageContext: PageContext): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    for (statementGroup <- page.wikiDataItem.getStatementGroups) {
      val references = statementGroup.getStatements.get(0).getReferences
      val property = statementGroup.getStatements.get(0).getClaim().getMainSnak().getPropertyId().toString.replace("(property)","").trim
      if (!references.isEmpty) {
       for (i <- references.indices) {
         for (reference <- references.get(i).getAllSnaks){
           val referenceProperty = reference.getPropertyId.toString.replace("(property)", "").trim
           val reference_id = subjectUri + "_"+property.replace("http://data.dbpedia.org/resource/","") .trim+ "_" + referenceProperty.replace("http://data.dbpedia.org/resource/","").trim
           quads += new Quad(context.language, WikidataReferenceDataSet, subjectUri, property, reference_id, page.wikiPage.sourceUri, null)
           reference match {
             case snak:ValueSnak => {
               quads += new Quad(context.language, WikidataReferenceDataSet, reference_id, referenceProperty, snak.getValue.toString, page.wikiPage.sourceUri, context.ontology.datatypes("xsd:string"))
             }
           }
         }
       }
      }

    }
  quads
 }
}
