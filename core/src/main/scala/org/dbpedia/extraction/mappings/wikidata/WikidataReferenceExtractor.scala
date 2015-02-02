package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.util.{Language, WikidataUtil}
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

class WikidataReferenceExtractor(
                                  context: {
                                    def ontology: Ontology
                                    def language: Language
                                  }
                                  )
  extends JsonNodeExtractor {

  val WikidataReferenceDataSet = new Dataset("wikidata-references")
  override val datasets = Set(WikidataReferenceDataSet)

  override def extract(page: JsonNode, subjectUri: String, pageContext: PageContext): Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]()

    for (statementGroup <- page.wikiDataItem.getStatementGroups) {
      statementGroup.getStatements.foreach {
        statement => {
          val references = statement.getReferences
          val property = WikidataUtil.replacePropertyId(statement.getClaim().getMainSnak().getPropertyId().toString)
          if (!references.isEmpty) {
            for (i <- references.indices) {
              for (reference <- references.get(i).getAllSnaks) {
                val referenceProperty = WikidataUtil.replacePropertyId(reference.getPropertyId.toString)
                val reference_id = subjectUri + "_" + property.replace("http://data.dbpedia.org/resource/", "").trim + "_" +
                  referenceProperty.replace("http://data.dbpedia.org/resource/", "").trim + "_ref"
                quads += new Quad(context.language, WikidataReferenceDataSet, subjectUri, property, reference_id, page.wikiPage.sourceUri, null)
                reference match {
                  case snak: ValueSnak => {
                    quads += new Quad(context.language, WikidataReferenceDataSet, reference_id, referenceProperty, snak.getValue.toString,
                      page.wikiPage.sourceUri, context.ontology.datatypes("xsd:string"))
                  }
                }
              }
            }
          }

        }

      }
    }
    quads
  }
}
