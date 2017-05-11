# TranskribusBaseLineEvaluationScheme
A tool for computing the qualitiy of baseline detection tools. Details explaining the evaluation scheme are available https://arxiv.org/abs/1705.03311 .
For a standalone jar, with examples and HowTo download [tool](https://github.com/Transkribus/TranskribusBaseLineEvaluationScheme/blob/master/TranskribusBaseLineEvaluationScheme_v0.1.1.tar.gz)

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
target/TranskribusBaseLineEvaluationScheme-x.x.x-jar-with-dependencies.jar
```
### Running
Have a look at the provided examples!  

If you only want to USE the tool run
```
java -jar target/TranskribusBaseLineEvaluationScheme-x.x.x-jar-with-dependencies.jar --help
```
for the help.

### Links
- https://transkribus.eu/TranskribusBaseLineEvaluationScheme/apidocs/index.html
