import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

name := """gatk_sbt_example"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "net.razorvine" % "pyrolite" % "4.7"

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("org.broadinstitute.gatk.queue.QCommandLine")


