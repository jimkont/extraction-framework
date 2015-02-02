package org.dbpedia.extraction.util

/**
 * Created by ali on 2/1/15.
 */
object WikidataUtil {
  def replacePunctuation(value:String,lang:String=""): String = {
    value.replace("(" + lang + ")", "").replace("[","").replace("]","").replace("\"","").trim()
  }

  def replacePropertyId(property:String):String = {
    property.replace("(property)", "").trim()
  }

  def replaceItemId(item:String):String= {
    item.replace("(item)","").toString().trim()
  }

  def replaceString(str:String):String = {
    str.replace("(String)","").trim()
  }

  val wikidataDBpNamespace = Language("wikidata").resourceUri.namespace
}
