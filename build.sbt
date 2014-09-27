name := "katyaformatter"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.7.3",
  anorm,
  cache
)     

play.Project.playScalaSettings
