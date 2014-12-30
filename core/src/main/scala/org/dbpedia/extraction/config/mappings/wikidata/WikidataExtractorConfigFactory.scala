package org.dbpedia.extraction.config.mappings.wikidata

import scala.util.matching.Regex
import scala.collection.mutable
import org.wikidata.wdtk.datamodel.interfaces.Value
/**
 * Created by ali on 12/20/14.
 */

trait WikidataExtractorConfig {
  def getCommand(property: String, value: Value, receiver: WikidataCommandReceiver): WikidataTransformationCommands
}

object WikidataExtractorConfigFactory {
  def createConfig(conf: String): WikidataExtractorConfig = {
    val selection = conf.toLowerCase()
    selection match {
      case selection if selection.endsWith(".txt") => {
        new TxtConfig(selection)
      }
    }
  }
}

class TxtConfig(filePath:String) extends WikidataExtractorConfig {
  val configMap = readConfiguration(filePath)

  private final def readConfiguration(filePath: String): mutable.Map[String, mutable.Map[String, String]] = {
    var configToMap = mutable.Map.empty[String, mutable.Map[String, String]]
    val source = scala.io.Source.fromFile(filePath)
    val configByLines = source.getLines.mkString

    val regex = """(.*?) [^\{\[]*[\{\[](((?<=\{)[^}]*)|((?<=\[)[^\]]*))[\}\]]""".r
    val line = regex findAllIn configByLines

    line.matchData foreach {
      m => {
        val property = m.group(1)
        val propertyValuePairs = m.group(2)
        var tempMap = mutable.Map.empty[String, String]
        propertyValuePairs match {
          case propertyValuePairs if propertyValuePairs.contains("{") => {
            //println(propertyValuePairs)
            val regex = """((?<=\{)[^}]*)""".r
            val allPairs = regex findAllIn propertyValuePairs
            allPairs.matchData foreach {
              n => tempMap += splitToTuple(n.group(0))
            }
            configToMap += (property -> tempMap)
          }
          case _ => {
            tempMap += splitToTuple(propertyValuePairs)
            configToMap += (property -> tempMap)
          }
        }
      }

    }
    source.close()
    //    println(configToMap)
    configToMap
  }

  private def splitToTuple(s: String): (String, String) = {
    val newPropertyValueSplitted = s.split(",")
    var newPropertyValueMap = ("", "")
    if (newPropertyValueSplitted.length > 1) {
      newPropertyValueMap = (newPropertyValueSplitted(0) -> newPropertyValueSplitted(1))
    } else {
      newPropertyValueMap = (newPropertyValueSplitted(0) -> null)
    }
    newPropertyValueMap
  }

  private def getMap(property: String): mutable.Map[String, String] = configMap.get(property) match {
    case Some(map) => map
    case None => mutable.Map.empty
  }

  def getCommand(property: String, value: Value, receiver: WikidataCommandReceiver): WikidataTransformationCommands = {
    var command = new WikidataTransformationCommands {
      override def execute():Unit=print("")
    }

    if (getMap(property).size == 1) {
      receiver.setParameters(property, value, getMap(property))
      val oneToOneCommand = new WikidataOneToOneCommand(receiver)
      command = oneToOneCommand
    } else if (getMap(property).size > 1) {
      receiver.setParameters(property, value, getMap(property))
      val oneToManyCommand = new WikidataOneToManyCommand(receiver)
      command = oneToManyCommand
    }
    command
  }
}