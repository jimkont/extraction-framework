package org.dbpedia.extraction.config.mappings

import org.dbpedia.extraction.ontology.{DBpediaNamespace, Ontology, OntologyProperty}


object WikidataExtractorConfig{

  val mapping = Map(
    "P19" -> new WdtkMapping(propertyTransformations =  List(new ReplaceFunction("dbo:birthPlace")), objectTransformations = List.empty),
    "P434" -> new WdtkMapping(propertyTransformations = List(new ReplaceFunction(("owl:sameAs"))), objectTransformations = List.empty)
  )

  val prefixMapping = Map(
    "P434" -> "http://musicbrainz.org/artist/"
  )

  object Use extends Enumeration {
    type Use = Value
    val rdfType= Value("rdf:type")
    val geoSpatialThing = Value("geo:SpatialThing")
    val geoLat = Value("geo:lat")
    val geoLong = Value("geo:long")
    val georss = Value("georss:point")
  }


}

trait TRansformationFunction {

}

class WdtkMapping(val propertyTransformations:List[TRansformationFunction],val objectTransformations: List[TRansformationFunction]) {
  def applyAllpropertyTransformations = {
  }

  def applyAllObectTransformations = {

  }
}

class ReplaceFunction(val newProperty:String) extends TRansformationFunction {
  override def toString: String=
    newProperty
}

class AddPrefix (val prefix:String, val value:String) extends TRansformationFunction {
  override def toString: String =
    prefix+value
}


