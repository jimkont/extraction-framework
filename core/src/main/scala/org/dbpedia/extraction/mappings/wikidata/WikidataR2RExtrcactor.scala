package org.dbpedia.extraction.mappings

import org.dbpedia.extraction.config.mappings.{ReplaceFunction, WikidataExtractorConfig}
import org.dbpedia.extraction.destinations.{Dataset, Quad}
import org.dbpedia.extraction.mappings.{JsonNodeExtractor, PageContext}
import org.dbpedia.extraction.ontology.Ontology
import org.dbpedia.extraction.util.Language
import org.dbpedia.extraction.wikiparser.JsonNode
import org.wikidata.wdtk.datamodel.interfaces._

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls

/**
 * Created by ali on 10/26/14.
 */
class WikidataR2RExtrcactor(
                               context : {
                                      def ontology : Ontology
                                      def language : Language
                                    }
                            )
  extends JsonNodeExtractor
{

  // this is where we will store the output
  val WikidataTestDataSet = new Dataset("wikidata-r2r")
  override val datasets = Set(WikidataTestDataSet)


  override def extract(page : JsonNode, subjectUri : String, pageContext : PageContext): Seq[Quad] =
  {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()
//    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
//
////      val claim = statementGroup.getStatements().get(0).getClaim()
////      val property = claim.getMainSnak().getPropertyId().toString().replace("(PropertyId)", "")
////      val propID=property.replace("http://data.dbpedia.org/resource/","")
////      claim.getMainSnak() match {
////        case mainSnak: ValueSnak => {
////          mainSnak.getValue() match {
////            case value: ItemIdValue => {
////                 val p = WikidataExtractorConfig.mapping().get("P19")
////                 println(p)
//////                val o =
////                //quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, null)
////
////            }
////            case _ =>
////          }
////        }
////        case _ =>
////      }
//    }
    quads
  }

}

