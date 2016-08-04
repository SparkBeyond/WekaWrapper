name := "wekaWrapper"

version := "0.3.0"

packAutoSettings

libraryDependencies += "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.12"
libraryDependencies += "commons-lang" % "commons-lang" % "2.6"
libraryDependencies += "tw.edu.ntu.csie" % "libsvm" % "3.1"
libraryDependencies += "tw.edu.ntu.csie" % "libsvm" % "3.1"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"
//libraryDependencies += "de.bwaldvogel" % "liblinear" % "1.95"

scalaVersion := "2.11.8"
