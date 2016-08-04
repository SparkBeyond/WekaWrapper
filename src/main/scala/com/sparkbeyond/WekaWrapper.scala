package com.sparkbeyond

import java.io.{BufferedWriter, FileWriter, File}

import org.apache.commons.lang.StringUtils
import weka.classifiers.Classifier
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils.DataSource

import scala.collection.mutable.ArrayBuffer

object WekaWrapper {

  val usage = """ADD the Usage text"""

  def buildOptionMap(map : Map[String, String], list: List[String]) : Map[String, String] = {
    def isSwitch(s : String) = (s(0) == '-')
    list match {
      case Nil => map
      case "-f" :: value :: tail =>
        buildOptionMap(map ++ Map("inputFile" -> value), tail)
      case "-d" :: value :: tail =>
        buildOptionMap(map ++ Map("outputModel" -> value), tail)
      case "-n" :: value :: tail =>
        buildOptionMap(map ++ Map("isNominal" -> value), tail)
      case "-nc" :: value :: tail =>
        buildOptionMap(map ++ Map("numberOfClasses" -> value), tail)
      case "-m" :: value :: tail =>
        buildOptionMap(map ++ Map("modelName" -> value), tail)
      case "-dist" :: value :: tail =>
        buildOptionMap(map ++ Map("distributionsOutput" -> value), tail)
      case "-options"  :: value :: tail =>
        buildOptionMap(map ++ Map("options" -> value), tail)
      case "-cv" :: value :: tail =>
        buildOptionMap(map ++ Map("classValues" -> value), tail)
      case "-oc" :: value :: tail =>
        buildOptionMap(map ++ Map("onlyClass" -> value), tail)
      case option :: value :: tail => {
        println("Unknown option " + option)
        buildOptionMap(map, tail)
      }
      case string :: Nil => {
        println("not allowed parameter: " + string)
        map
      }
    }
  }

  def  main(args: Array[String]) {

    if (args.length == 0) {
      println(usage)
      System.exit(1)
    }
    val argsList = args.toList
    val action = argsList(0)
    if (action !="TrainModel" && action !="Classify") {
        println(usage)
        System.exit(1)
      }

    val optionMap = buildOptionMap(Map(), argsList.drop(1))

    if (action == "TrainModel") {

      val filePath = optionMap.get("inputFile")
      val outputPath = optionMap.get("outputModel")
      val isNominal = optionMap.get("isNominal")
      val modelName = optionMap.get("modelName")
      val options = optionMap.getOrElse("options",null)
      val classValues = optionMap.get("classValues")

      val optsionsArr = if (options == null) null else weka.core.Utils.splitOptions(options)

      if(filePath.isEmpty || outputPath.isEmpty || isNominal.isEmpty || modelName.isEmpty || (isNominal.head.toBoolean && classValues.isEmpty) ) {
        println(usage)
        System.exit(1)
      }
      val nominal = isNominal.head.toBoolean

      val trainReader = InputTrainReader(filePath.head)
      val labelNames = if (nominal) StringUtils.splitPreserveAllTokens(classValues.head, '%').toSeq else Seq()
      val attrs = DataProcess.buildWekaAttributes(trainReader.headers, trainReader.attributes, nominal, labels = Some(labelNames))
      val instances = DataProcess.makeLabeledInstances("temp", trainReader.parseFeatureValues(), trainReader.numberDataOfLines, attrs, nominal)
      val model = Classifier.forName(modelName.head,optsionsArr)
      model.buildClassifier(instances)
      val algoName = if (options == null) modelName.head else s"${modelName.head}(${options})"
      println(s"Model: $algoName")
      println(model.toString)
      weka.core.SerializationHelper.write(outputPath.head, model)
    }

    if (action == "Classify") {

      val testFilePath = optionMap.get("inputFile")
      val inputModelPath = optionMap.get("outputModel")
      val isNominal = optionMap.get("isNominal")
      val distOutput = optionMap.get("distributionsOutput")
      val numOfClasses = optionMap.getOrElse("numberOfClasses","2").toInt
      val onlyClass = optionMap.getOrElse("onlyClass", "false").toBoolean


      if(testFilePath.isEmpty || inputModelPath.isEmpty || isNominal.isEmpty || distOutput.isEmpty) {
        println(usage)
        System.exit(1)
      }

      println(s"Classifying instances from ${testFilePath.head} based on model from ${inputModelPath.head}")

      val model = weka.core.SerializationHelper.read(inputModelPath.head).asInstanceOf[Classifier]
      val testReader = InputTestReader(testFilePath.head)
      val attrs = DataProcess.buildWekaAttributes(testReader.headers, testReader.attributes, isNominal.head.toBoolean,numOfClasses)
      val instances = DataProcess.makeUnlabeledInstances("temp", testReader.parseFeatureValues(), testReader.numberDataOfLines, attrs)
      val distributions = new ArrayBuffer[Array[Double]](instances.numInstances())
      if(onlyClass)
      {
        (0 until instances.numInstances()).view.foreach {
          i => distributions += Array(model.classifyInstance(instances.instance(i)))
        }
      }
      else {
        (0 until instances.numInstances()).view.foreach {
          i => distributions += model.distributionForInstance(instances.instance(i))
        }
      }

      val writer = new BufferedWriter(new FileWriter(distOutput.head))
      distributions.foreach(dist => writer.write(dist.mkString("\t") + "\n"))
      writer.flush()
      writer.close()

    }



  }
}