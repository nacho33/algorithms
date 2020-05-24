name := "algorithms"

version := "0.1"

scalaVersion := "2.12.7"
lazy val akkaVersion = "2.5.13" // must be 2.5.13 so that it's compatible with the stores plugins (JDBC and Cassandra)

// some libs are available in Bintray's JCenter
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
)