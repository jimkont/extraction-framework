package org.dbpedia.extraction.mappings

import java.net.URI

import org.dbpedia.extraction.wikiparser.{TemplateNode,WikiTitle}
import org.dbpedia.extraction.ontology.datatypes.Datatype
import org.dbpedia.extraction.destinations.{DBpediaDatasets, Quad}
import org.dbpedia.extraction.ontology.{OntologyProperty, OntologyObjectProperty}
import org.dbpedia.extraction.util.{WikiUtil, Language}
import scala.language.reflectiveCalls

/**
 * Used to map information that is only contained in the infobox template name, for example
 *
 * en:Infobox_Australian_Road
 * {{TemplateMapping
 *    | mapToClass = Road
 *    | mappings =
 *         {{ConstantMapping | ontologyProperty = country | value = Australia }}
 *   ...
 * }}
 */
class ConstantMapping (
  ontologyProperty: OntologyProperty,
  private var value : String,
  datatype : Datatype,
  context : {
    def language : Language
  } 
)
extends PropertyMapping
{
  if (ontologyProperty.isInstanceOf[OntologyObjectProperty])
  {
    require(datatype == null, "expected no datatype for object property '"+ontologyProperty+"', but found datatype '"+datatype+"'")
    value = try {
      // if it is a URI return it directly
      val uri = new URI(value)
      // if the URI is absolute, we can use it directly. otherwise we make a DBpedia resource URI
      if (uri.isAbsolute) uri.toString
      else getDBpediaURI(value,context.language)
    } catch {
      // otherwise create a DBpedia resource URI
      case _ : Exception => getDBpediaURI(value,context.language)
    }
  }

  override val datasets = Set(DBpediaDatasets.OntologyProperties)

  override def extract(node : TemplateNode, subjectUri : String, pageContext : PageContext) : Seq[Quad] =
  {
    Seq(new Quad(context.language, DBpediaDatasets.OntologyProperties, subjectUri, ontologyProperty, value, node.sourceUri, datatype))
  }

  def getDBpediaURI(value: String, language: Language): String = {
    val title = WikiTitle.parse(value, language)
    language.resourceUri.append(title.decodedWithNamespace)
  }


}