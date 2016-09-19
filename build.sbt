name := "slack-lambda"

val commonSettings = Seq(
  organization := "io.valadan.slack-lambda",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.11.7",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
)

lazy val lambdaDependencies = Seq (
  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.github.gilbertw1" %% "slack-scala-client" % "0.1.8"
)

lazy val lambda = project.in(file("."))
  .enablePlugins(AwsLambdaPlugin)
  .settings(commonSettings:_*)
  .settings(libraryDependencies ++= lambdaDependencies)

lambdaHandlers := Seq(
  "slack-lambda" -> "io.valadan.slacklambda.SlackLambdaHandler::handle"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

s3Bucket := Some("slack-lambda")

awsLambdaMemory := Some(512)

awsLambdaTimeout := Some(30)

roleArn := Some("arn:aws:iam::552639231567:role/lambda_basic_execution")
