name := "LiftMicroservices"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
    "org.scala-lang"        % "scala-reflect"           % scalaVersion.value,
    "org.scala-lang"        % "scala-compiler"          % scalaVersion.value,
    "net.liftweb"           %% "lift-webkit"            % "2.6.3",
    "ch.qos.logback"        % "logback-classic"         % "1.1.6",
    "com.papertrailapp"     % "logback-syslog4j"        % "1.0.0",
    "com.amazonaws"         % "aws-java-sdk-cloudwatch" % "1.10.58",
    "com.blacklocus"        % "metrics-cloudwatch"      % "0.4.0",
    "io.igl"                %% "jwt"                    % "1.2.0",
    "org.specs2"            %% "specs2-core"            % "3.7.2"       % "test",
    "org.specs2"            %% "specs2-scalacheck"      % "3.7.2"       % "test",
    "org.specs2"            %% "specs2-mock"            % "3.7.2"       % "test",
    "org.scalacheck"        %% "scalacheck"             % "1.12.5"      % "test",
    "com.h2database"        % "h2"                      % "1.4.191",
    "org.springframework"   % "spring-mock"             % "2.0.8",
    "org.springframework"   % "spring-core"             % "2.0.8",
    "javax.servlet"         % "javax.servlet-api"       % "3.1.0",
    "com.typesafe.akka"     %% "akka-actor"             % "2.4.4",
    "com.typesafe.akka"     %% "akka-http-experimental" % "2.4.4"
)

enablePlugins(JettyPlugin)

coverageExcludedPackages := "<empty>;bootstrap.liftweb.Boot.*"
