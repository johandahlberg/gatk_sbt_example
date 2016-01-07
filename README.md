What is this?
=============

An example for @dalk of how to setup a GATK Queue based project using sbt as the build tool. 

Instructions
============
Note this requires sbt 0.13.5 or higher to be installed.

 - Get the Queue jar (download or build from source).
 - `mkdir lib`
 - Place the Queue jar in `lib/`
 - Run `sbt universal:packageBin`

 Now you will have a deployable zip-file under `target/universal/gatk_sbt_example-1.0.zip` with a generated bash-script that you can run to launch the application.

For example:

    unzip target/universal/gatk_sbt_example-1.0.zip
    ./gatk_sbt_example-1.0/bin/gatk_sbt_example --help

This used sbt-native-packager to create the package. To read more about how to use this nice plugin go to: http://www.scala-sbt.org/sbt-native-packager


