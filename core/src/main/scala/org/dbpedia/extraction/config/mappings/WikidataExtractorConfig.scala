package org.dbpedia.extraction.config.mappings

import scala.collection.mutable


/**
 * Created by ali on 11/22/14.
 */


//Test class of client
object WikidataExtractorConfig{

  def conf(property:String="", value:String="", appliesTo:String):mutable.Map[String,String]= {
    //Sample Config files
    val configFileExample = List("replace property P19 dbo:birthPlace", "addprefix value P21 http://example.com/")

    var testData=""
    if (appliesTo=="property") testData=property
    else testData=value

    //println(testData)
    val result:WikidataMapping=new WikidataMapping(testData, appliesTo)
    val myMapping:WdtkMapping = new WdtkMapping()

    for (eachLine <- configFileExample ) {
      configSplit(eachLine)
      val eachCmd = configSplit.eachCmd
      val confProperty=configSplit.property
      val applies=configSplit.appliesTo
      val newParam = configSplit.newParam

        //Create all avialable commands
     val repCommand:ReplaceFunction = new ReplaceFunction(result)
     val addPrefix:AddPrefixFunction = new AddPrefixFunction(result)

      eachCmd match {
        case "replace" => {
          if (property==confProperty && appliesTo==applies){
            result.setReplaceParameters(confProperty,newParam)
            myMapping.executeCommand(repCommand)
          }
        }
        case "addprefix" => {
          if (property==confProperty && appliesTo == applies){
            result.setPrefix(newParam)
            myMapping.executeCommand(addPrefix)
          }
        }
        case _=> // do nothing
      }
    }
//    println(result.testMap)
    result.testMap
  }

  object configSplit {
    var eachCmd=""
    var appliesTo=""
    var property=""
    var newParam =""
    def apply(confLine:String): Unit = {
      val confLineSplit=confLine.split("\\s+")
      eachCmd = confLineSplit(0)
      appliesTo=confLineSplit(1)
      property=confLineSplit(2)
      newParam=confLineSplit(3)
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


  val checkPoint = OldString

  def setPrefix(prefix:String): Unit ={
    this.prefix=prefix
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

