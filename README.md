# TranskribusBaseLineEvaluationScheme
A tool for computing the qualitiy of baseline detection tools for more details see the following [draft](https://github.com/Transkribus/TranskribusBaseLineEvaluationScheme/blob/master/line_metric_draft.pdf)

[![Build Status](http://dbis-halvar.uibk.ac.at/jenkins/buildStatus/icon?job=TranskribusBaseLineEvaluationScheme)](http://dbis-halvar.uibk.ac.at/jenkins/job/TranskribusBaseLineEvaluationScheme)

### Requirements
- Java >= version 7
- Maven
- All further dependencies are gathered via Maven

### Build Steps
```
git clone https://github.com/Transkribus/TranskribusBaseLineEvaluationScheme
cd TranskribusBaseLineEvaluationScheme
mvn install
```
a stand-alone tool is created at
```
target/TranskribusBaseLineEvaluationScheme-0.1.0-jar-with-dependencies.jar
```
### Running
See source code of
eu.transkribus.baselineevaluationscheme.Metric_BL_run
how the tool works.

If you only want to USE the tool run
```
java -jar target/TranskribusBaseLineEvaluationScheme-0.1.0-jar-with-dependencies.jar --help
```
for the help.

### Links
- https://transkribus.eu/TranskribusBaseLineEvaluationScheme/apidocs/index.html
