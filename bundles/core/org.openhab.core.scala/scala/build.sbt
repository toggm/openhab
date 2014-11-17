
name := "openhab-scala-lib"

version := "1.0"

scalaVersion := "2.9.0"

libraryDependencies ++= Seq(
	"org.specs2" %% "specs2" % "1.12.4"  % "test",
	"org.scala-tools.testing" %% "specs" % "1.6.8" % "test",
	"org.slf4j" % "slf4j-api" % "1.7.5",
	"org.slf4j" % "slf4j-simple" % "1.7.5"  % "test",
	"joda-time" % "joda-time" % "2.1" % "test",
	"org.eclipse.osgi" % "org.eclipse.osgi.services" % "3.2.100.v20100503" % "test",	
	"org.joda" % "joda-convert" % "1.5" % "test", 
	"commons-collections" % "commons-collections" % "2.0" % "test"
)