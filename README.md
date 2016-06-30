# TranskribusBaseLineMetricTool
A tool for computing the qualitiy of baseline tools towards groundtruth. TODO: more exactly...

[![Build Status](http://dbis-halvar.uibk.ac.at/jenkins/buildStatus/icon?job=TranskribusBaseLineMetricTool)](http://dbis-halvar.uibk.ac.at/jenkins/job/TranskribusBaseLineMetricTool)

### Requirements
- Java >= version 7
- Maven
- All further dependencies are gathered via Maven

### Build Steps
```
git clone https://github.com/Transkribus/TranskribusBaseLineMetricTool
cd TranskribusBaseLineMetricTool
mvn install
```
### Running
See source code of
eu.transkribus.baselinemetrictool.Metric_BL_run
how the tool works.

Use
```
java -jar java -jar target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar
```
or
```
java -jar java -jar target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar src/test/resources/truth.lst src/test/resources/reco.lst -p -tol -i src/test/resources/metrEx.png
```
to see an example application.

Use
```
java -jar ~/.m2/repository/eu/transkribus/TranskribusBaseLineMetricTool/0.0.1/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar --help
```
or  
```
java -jar target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar --help
```
to use tool for other lists.

### Links
- https://transkribus.eu/TranskribusBaseLineMetricTool/apidocs/index.html
