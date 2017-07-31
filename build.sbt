name := "kafka-consumer-offsets-to-json-topic"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies += "org.apache.kafka" % "kafka-streams" % "0.11.0.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "0.11.0.0"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.0"
libraryDependencies += "org.rogach" %% "scallop" % "3.0.3"
