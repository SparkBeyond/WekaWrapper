package com.sparkbeyond

import java.io.{FileInputStream, BufferedInputStream}
import org.apache.commons.lang.StringUtils

import scala.io.Source

case class FileData(features: Array[String], label:Option[String] , weight: Option[String])

trait InputReader {

  val inputFile: String
  lazy val numberDataOfLines = Source.fromInputStream(new BufferedInputStream(new FileInputStream(inputFile), 100 * 1024)).getLines().size - 2
  val lines = Source.fromInputStream(new BufferedInputStream(new FileInputStream(inputFile), 100 * 1024)).getLines()
 // lazy val lines = Source.fromInputStream(new BufferedInputStream(new FileInputStream(inputFile), 100 * 1024)).getLines().toList
  def headers() : Array[String]
  def attributes() : Array[String]

 // def labels() : Array[Option[String]]
 // lazy val headers: Array[String] = processLine(lines(0))
 // lazy val attributes: Array[String] = processLine(lines(1))
 // lazy val labels: Array[Option[String]] = parseFeatureValues.map(fd => fd.label).toArray

  def parseFeatureValues() : Iterator[FileData] = {
    lines.map(line => splitFeaturesValuesWeights(processLine(line)))
  }
  def processLine(line: String) = {
    StringUtils.splitPreserveAllTokens(line, '\t')
  }

  def splitFeaturesValuesWeights(line: Array[String]) : FileData
}

case class InputTrainReader (inputFile: String) extends InputReader {

//   lazy val headers: Array[String] = processLine(lines(0)).dropRight(2)
//   lazy val attributes: Array[String] = processLine(lines(1)).dropRight(2)
  // lazy val labels: Array[Option[String]] = parseFeatureValues.map(fd => fd.label).toArray

  val headers: Array[String] = processLine(lines.next).dropRight(2)
  val attributes: Array[String] = processLine(lines.next).dropRight(2)

  def splitFeaturesValuesWeights(line: Array[String]) = {
    val valWeight = line.takeRight(2)
    FileData(line.dropRight(2), Some(valWeight(0)), Some(valWeight(1)))
  }
}

case class InputTestReader (inputFile: String) extends InputReader {

//lazy val headers: Array[String] = processLine(lines(0))
//  lazy val attributes: Array[String] = processLine(lines(1))
 // lazy val labels: Array[Option[String]] = parseFeatureValues.map(fd => fd.label).toArray

  val headers: Array[String] = processLine(lines.next)
  val attributes: Array[String] = processLine(lines.next)

  def splitFeaturesValuesWeights(line: Array[String]) = {
    FileData(line, None, None)
  }

}
