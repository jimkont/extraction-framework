package org.dbpedia.extraction.config.mappings

import scala.collection.mutable


/**
 * Created by ali on 11/22/14.
 */


//Test class of client
object WikidataExtractorConfig{

  //Sample Config files
  val configFileExample = mutable.MutableList(
    "replace property http://data.dbpedia.org/resource/P434 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P435 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P436 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P982 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P966 owlSameAs",
    "replace property http://data.dbpedia.org/resource/1004 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P661 owlSameAs",
    "replace property http://data.dbpedia.org/resource/P646 owlSameAs",

    "addprefix value http://data.dbpedia.org/resource/P646 owlSameAs http://freebase.com",
    "addprefix value http://data.dbpedia.org/resource/P434 owlSameAs http://musicbrainz.org/artist/",
    "addprefix value http://data.dbpedia.org/resource/P435 owlSameAs http://musicbrainz.org/work/",
    "addprefix value http://data.dbpedia.org/resource/P436 owlSameAs http://musicbrainz.org/release-group/",
    "addprefix value http://data.dbpedia.org/resource/P982 owlSameAs http://musicbrainz.org/area/",
    "addprefix value http://data.dbpedia.org/resource/P966 owlSameAs http://musicbrainz.org/label/",
    "addprefix value http://data.dbpedia.org/resource/P1004 owlSameAs http://musicbrainz.org/place/",
    "addprefix value http://data.dbpedia.org/resource/P661 owlSameAs http://rdf.chemspider.com/",

    "replace property http://data.dbpedia.org/resource/P625 rdfType",
    "replace value http://data.dbpedia.org/resource/P625 rdfType spatialThing",
    "replace property http://data.dbpedia.org/resource/P625 geoLat",
    "replace value http://data.dbpedia.org/resource/P625 geoLat newValue1",
    "replace property http://data.dbpedia.org/resource/P625 geoLong",
    "replace value http://data.dbpedia.org/resource/P625 geoLong newValue2",
    "replace property http://data.dbpedia.org/resource/P625 geoRss",
    "replace value http://data.dbpedia.org/resource/P625 geoRss newValue12"
  )

  var MapResult=mutable.Map.empty[String,mutable.Map[String,String]]

  def conf(property:String="", value:String="", appliesTo:String):Unit= {

    var testData=""
    if (appliesTo=="property") testData=property
    else testData=value

    //println(testData)
    val result:WikidataMapping=new WikidataMapping(testData, appliesTo)
    val myMapping:WdtkMapping = new WdtkMapping()
    var newMap = mutable.Map.empty[String,String]

    for (eachLine <- configFileExample ) {
      configSplit(eachLine)
      val eachCmd = configSplit.eachCmd
      val confProperty=configSplit.property
      val applies=configSplit.appliesTo
      val newProperty = configSplit.newProperty
      val newValue = configSplit.newValue
        //Create all avialable commands
     val repCommand:ReplaceFunction = new ReplaceFunction(result)
     val addPrefix:AddPrefixFunction = new AddPrefixFunction(result)

      eachCmd match {
        case "replace" => {
          if (property==confProperty){
            if (applies == appliesTo && applies=="property") result.setReplaceParameters(confProperty,newProperty)
            else if (applies == appliesTo && applies=="value") result.setReplaceParameters(confProperty,newValue)
            myMapping.executeCommand(repCommand)
          }
        }
        case "addprefix" => {
          if (property==confProperty){
            if (applies == appliesTo && applies=="property") result.setPrefix(newProperty)
            else if (applies == appliesTo && applies=="value")result.setPrefix(newValue)
            myMapping.executeCommand(addPrefix)
          }
        }
        case _=> // do nothing
      }

      if (property==confProperty && newMap.contains(newProperty) && !result.testMap.get("value").isEmpty) newMap(newProperty) += result.testMap.get("value")
      else if (property==confProperty) newMap += (newProperty-> "")

      if (MapResult.contains(confProperty) && confProperty==property) MapResult += (property -> newMap)
      else MapResult += (property -> mutable.Map.empty)
    }
//    println(MapResult)
//    result.testMap
  }

  object configSplit {
    var eachCmd=""
    var appliesTo=""
    var property=""
    var newProperty =""
    var newValue=""
    def apply(confLine:String): Unit = {
      val confLineSplit=confLine.split("\\s+")
      eachCmd = confLineSplit(0)
      appliesTo=confLineSplit(1)
      property=confLineSplit(2)
      newProperty=confLineSplit(3)
      if (confLineSplit.length==5) newValue = confLineSplit(4)
      else newValue=""
    }
  }

}

//The Command Interface
trait TRansformationFunction {
  def execute()
}

//The Invoker Class
class WdtkMapping() {

  def executeCommand(cmd:TRansformationFunction): Unit = {
    cmd.execute()
  }
}


//The Receiver class
class WikidataMapping(testData:String, appliesTo:String="") {

  val testMap=mutable.Map.empty[String,String]
  var OldString:String=""
  var NewString:String=""
  var prefix =""
  var suffix =""

  val checkPoint = OldString

  def setPrefix(prefix:String): Unit ={
    this.prefix=prefix
  }

  def setSuffix(suffix: String): Unit = {
    this.suffix=suffix
  }

  def setReplaceParameters(oldStr:String,newString:String){
    OldString=oldStr
    NewString=newString
  }

  def replace (){
    testData match {
      case checkPoint => testMap +=(appliesTo -> NewString)
      case _=>
    }

  }

  def addPrefix() {
    testMap += (appliesTo -> (prefix + testData))
  }

  def addSuffix(): Unit = {
    testMap += (appliesTo -> (testData+ suffix))
  }
}

//Concrete command class
class ReplaceFunction(mapping:WikidataMapping) extends TRansformationFunction {
  //  def replace():String = newValue
  def execute(): Unit ={
    mapping.replace()
  }
}

//Concrete command class
class AddPrefixFunction (mapping:WikidataMapping) extends TRansformationFunction {
  def execute(): Unit = {
    mapping.addPrefix()
  }
}

class AddSuffixFunction (mapping:WikidataMapping) extends TRansformationFunction {
  def execute(): Unit = {
    mapping.addSuffix()
  }
}
