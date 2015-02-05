package org.dbpedia.extraction.util

import java.security.MessageDigest

import org.wikidata.wdtk.datamodel.interfaces.{ItemIdValue, Value}

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

  def getItemId(value:Value) = value match {
    case v:ItemIdValue => replaceItemId(v.toString).replace(wikidataDBpNamespace,"")
    case _ => getHashId(value)
  }

  def getStatementUri(subject:String, property:String,value:Value):String = {
    subject+"_"+ property.replace(WikidataUtil.wikidataDBpNamespace, "").trim+"_" + getItemId(value)
  }

  def splitQualifier(qualifier:String):Map[String,String] = {
    Map(qualifier.toString.split("::")(0).trim ->  qualifier.toString.split("::")(1).trim)
  }

  //Hash function take string, make SHA hash of it.
  def getHashId(value:Value):String={
    val hash_string = value.toString
    MessageDigest.getInstance("SHA").digest(hash_string.getBytes).toString
  }

  val wikidataDBpNamespace = Language("wikidata").resourceUri.namespace
}
