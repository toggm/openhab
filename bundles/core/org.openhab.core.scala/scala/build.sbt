
name := "openhab-scala-lib"

version := "1.0"

scalaVersion := "2.9.0"

libraryDependencies ++= Seq(
	"org.specs2" %% "specs2" % "1.12.4"  % "test",
	"org.scala-tools.testing" %% "specs" % "1.6.8" % "test",
	"org.slf4j" % "slf4j-simple" % "1.7.5"
)