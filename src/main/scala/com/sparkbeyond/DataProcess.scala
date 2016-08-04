package com.sparkbeyond

import weka.core.{Instance, Instances, Attribute, FastVector}


object AttrTypeHelper {
  val boolAttrFv = new FastVector(2)
  List("false", "true") foreach boolAttrFv.addElement

    def wekaAttribute(name: String, attr: String) = attr match {
      case "BooleanAttr" => new Attribute(name, boolAttrFv)
      case "NumericAttr" => new Attribute(name)
      case "DoubleAttr" => new Attribute(name)
      case "IntAttr" => new Attribute(name)
      case _ => throw new RuntimeException("This attribute type is not supported: " + attr)
    }
}

object DataProcess {

     def buildWekaAttributes(attributeNames: Seq[String], featureTypes: Seq[String], isLabelNominal: Boolean, numOfClasses: Int = 0, labels: Option[Seq[String]] = None) = {
       val featureCount = featureTypes.size

       val fvWekaAttributes = new FastVector(featureCount + 1)
       attributeNames.zip(featureTypes).foreach { case (name, ftyp) => fvWekaAttributes.addElement(AttrTypeHelper.wekaAttribute(name, ftyp)) }

       if (isLabelNominal) {
         val lablesDistinct = if (labels.isDefined) labels.head else (0 until numOfClasses).map(n => ("class" + n.toString)).toSeq
         val classAttrFv = new FastVector(lablesDistinct.size)
         lablesDistinct.foreach(l => classAttrFv addElement l)
         fvWekaAttributes addElement new Attribute("class", classAttrFv)

       } else {
         fvWekaAttributes addElement new Attribute("class")
       }
       fvWekaAttributes
     }

    def makeLabeledInstances(name: String, data: Iterator[FileData], dataSize: Int, attrs: FastVector, isLabelNominal: Boolean = true) = {
      val res = new Instances(name, attrs, dataSize)
      val attrsArr = attrs.toArray.map(_.asInstanceOf[Attribute])
      res.setClassIndex(attrs.size - 1)
      data foreach { fdata => res.add(makeLabeledInstance(attrsArr, fdata.features, fdata.label.head, if (fdata.weight.isDefined) fdata.weight.head.toDouble else 1.0, isLabelNominal))}
      res
    }

  def makeUnlabeledInstances(name: String, data: Iterator[FileData], dataSize: Int, attrs: FastVector) = {
    val res = new Instances(name, attrs, dataSize)
    val attrsArr = attrs.toArray.map(_.asInstanceOf[Attribute])
    data foreach { fdata => res.add(makeUnlabeledInstance(attrsArr, fdata.features))}
    res.setClassIndex(attrs.size - 1)
    res
  }

    def makeUnlabeledInstance(attrs: Array[Attribute], features: Seq[String]) = {
      val arr = new Array[Double](attrs.size)
      var counter = 0
      for (f <- features.take(attrs.size - 1)) {
        arr(counter) = matchDoubleToFeatureValue(f)
         counter += 1
      }
      arr(counter) = Instance.missingValue()
      new Instance(1, arr)
    }

  def makeLabeledInstance(attrs: Array[Attribute], features: Seq[String], label: String, weight: Double = 1.0, isLabelNominal: Boolean = true) = {
    val arr = new Array[Double](attrs.size)
    var counter = 0
    for (f <- features.take(attrs.size - 1)) {
      arr(counter) = matchDoubleToFeatureValue(f)
      counter += 1
    }
    val instance = new Instance(weight, arr)
    try {
      if (isLabelNominal)
        instance.setValue(attrs(counter), label.toString)
      else
        instance.setValue(attrs(counter), label.toDouble)
    } catch {
      case e: Throwable =>
        println(s"failed setting value ${attrs(counter)}")
        throw e
    }
    instance
  }

  def matchDoubleToFeatureValue(feature: String): Double =
  {
    feature.toLowerCase match {
      case "true" => 1.0
      case "false" => 0.0
      case "nan" => Instance.missingValue()
      case "infinity" => Double.MaxValue
      case "-infinity" => Double.MinValue
      case x: Any => x.toDouble
    }
  }


}
