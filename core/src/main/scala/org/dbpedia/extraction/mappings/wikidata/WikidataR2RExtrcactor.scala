package org.dbpedia.extraction.mappings

import com.sun.xml.internal.ws.addressing.policy.AddressingPrefixMapper
import org.dbpedia.extraction.config.mappings.{WdtkMapping, AddPrefix, WikidataExtractorConfig}
import org.dbpedia.extraction.mappings.{PageContext, JsonNodeExtractor}
import org.dbpedia.extraction.ontology.{OntologyProperty, Ontology}
import org.dbpedia.extraction.util.Language
import org.dbpedia.extraction.destinations.{Dataset, Quad, DBpediaDatasets}
import org.dbpedia.extraction.wikiparser.{JsonNode}
import org.wikidata.wdtk.datamodel.interfaces._
import collection.mutable.ArrayBuffer
import scala.language.reflectiveCalls
import scala.collection.JavaConversions._

import scala.collection.mutable.ArrayBuffer

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
  val WikidataTestDataSet = new Dataset("wikidata-geo")
  override val datasets = Set(WikidataTestDataSet)


  val simpleTranformProperties=List("P434", "P435", "P436", "P982", "P966", "P1004", "P686", "P661", "P227","P244",
    "P269", "P349","P646", "P638", "P662", "P685","P214")

  override def extract(page : JsonNode, subjectUri : String, pageContext : PageContext): Seq[Quad] =
  {
    // This array will hold all the triples we will extract
    val quads = new ArrayBuffer[Quad]()

    for ((statementGroup) <- page.wikiDataItem.getStatementGroups) {
      val claim = statementGroup.getStatements().get(0).getClaim()
      val property = claim.getMainSnak().getPropertyId().toString().replace("(PropertyId)", "")
      val propID=property.replace("http://data.dbpedia.org/resource/","")

      claim.getMainSnak() match {
        case mainSnak: ValueSnak => {
          mainSnak.getValue() match {
            case value: ItemIdValue => {
              if (simpleTranformProperties.contains(propID)){
                val p = WikidataExtractorConfig.mapping(propID)
                val o = new WdtkMapping(propertyTransformations = List.empty, objectTransformations = List(new AddPrefix(WikidataExtractorConfig.prefixMapping(propID), value.toString)))
                //quads += new Quad(context.language, WikidataTestDataSet, subjectUri, p, o, page.wikiPage.sourceUri, null)
              }

            }
            case _ =>
          }
        }
        case _ =>
      }
    }
    quads
  }

}

