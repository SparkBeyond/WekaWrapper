name := "wekaWrapper"

version := "0.2.2"

packAutoSettings

libraryDependencies += "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.12"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
libraryDependencies += "tw.edu.ntu.csie" % "libsvm" % "3.1"
//libraryDependencies += "de.bwaldvogel" % "liblinear" % "1.95"

scalaVersion := "2.11.7"
