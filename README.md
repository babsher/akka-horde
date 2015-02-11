# Zerg Horde (AKA Akka Horde)

## Required Software 

* java 7 (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* sbt (http://www.scala-sbt.org/)
* node js (http://nodejs.org/)
* node package manager, should come with node js

## How to build
### Build Website
```bash
cd  src/main/resources/app
```

```bash
sudo npm install -g yo gulp bower
```

```bash
npm install
```

```bash
bower install
```

```bash
gulp build
```
### Build Jar
```bash
sbt build
```
