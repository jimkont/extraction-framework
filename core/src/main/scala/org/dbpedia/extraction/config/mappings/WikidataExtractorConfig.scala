package org.dbpedia.extraction.config.mappings


object WikidataExtractorConfig{


  type Property = String
  type Value = String

  type PropertyMapping = Map[Property, Property]

  val mapping = Map(
    "P19" -> "http://dbpedia.org/property/birthPlace",
    "P6" -> "http://dbpedia.org/property/governmentHead",
    "P7" -> "http://dbpedia.org/property/brother",
    "P434" -> "owl:sameAs"
  )

  def replace(property: Property,mapping: PropertyMapping): Property = {
    mapping(property)
  }

  def addPrefix(prefix:Any, value:Value): Value = {
    return prefix+value
  }

  object Use extends Enumeration {
    type Use = Value
    val rdfType= Value("rdf:type")
    val geoSpatialThing = Value("geo:SpatialThing")
    val geoLat = Value("geo:lat")
    val geoLong = Value("geo:long")
    val georss = Value("georss:point")
  }


}

