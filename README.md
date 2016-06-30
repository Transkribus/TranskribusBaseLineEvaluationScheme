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
a stand-alone tool is created at
```
target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar
```
### Running
See source code of
eu.transkribus.baselinemetrictool.Metric_BL_run
how the tool works.

Use
```
java -jar target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar
```
see an example application (without parameters example-parameters are used).

Use
```
java -jar target/TranskribusBaseLineMetricTool-0.0.1-jar-with-dependencies.jar --help
```
to use the tool for other lists.

### Links
- https://transkribus.eu/TranskribusBaseLineMetricTool/apidocs/index.html
