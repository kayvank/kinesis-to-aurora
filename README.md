kinesis-to-aurora
====
This is an Idempotent Scala service that persists user-events from kinesis stream to Aurora RDS.

## Purpose
The main purpose of the project is to demonstrate 
- http4s
- kamon
- AWS kinesis & KCL
- scalaz streams & Task implementation for AWS Kinesis stream
- Scalaz Task
- scala & docker
- doobie  
- circe
- statsd 

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
What things you need to install the software and how to install them

#### Assumption
  * you have  
    1. [docker-id]
    2. [AWS account]

### Installing
- set up development environment 
  * [scala 2.11+](https://www.scala-lang.org/download/)
  * [sbt](http://www.scala-sbt.org/download.html)
  * [docker](https://docs.docker.com/)
  * [docker-compose](https://docs.docker.com/compose/)
  * [JDK 8+]
  * [direnv](https://direnv.net/)
  * *[aws-cli*]()
  * *[emacs ensime*](http://ensime.github.io//editors/emacs/)

- set up environmnet variables
```
export DOCKER_USER='docker_userid'
export DOCKER_PASS='docker-password'
export AWS_REGION='us-east-1'
export AWS_ACCESS_KEY_ID=XXXXXX
export SECRET_ACCESS_KEY=XXXXXX
export JDBC_USER=xxxxx
export JDBC_PASSWORD=xxxxx
export JDBC_DB=mytestdb
export JDBC_URL='mysql://$JDBC_USER:$JDBC_PASSWORD@0.0.0.0:3306/$JDBC_DB'

```

#### Setup Project
```
git clone git@github.com:kayvank/kinesis-to-aurora.git
cd kinesis-to-aurora
sbt clean compile  ## to build prject
sbt test           ## unit test
##
### create a zip file containing bash & DOS executables 
##

##
### create docker image and publish locally
##
sbt clean compile universal:packageBin

##
### create docker image and publish to docker-hub
##
sbt clean compile docker:publish
```

####  Running the Project locally
Instruction for running the project locally.
We are going to use mysql docker image to simulate AWS [Aurora RDS](http://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Welcome.html)
- Assumptions
  * you have created a AWS kinesis stream

- Setup database
```
cd ./scripts/sql
docker-compose -f ./docker-compose-mysql.yml up -d
mysql -h 0.0.0.0 -u $JDBC_USER -p $JDBC_DB < ./like_events.sql
```
- Setup statsd & graphite
```
docker run -d\
 --name graphite\
 --restart=always\
 -p 80:80\
 -p 2003-2004:2003-2004\
 -p 2023-2024:2023-2024\
 -p 8125:8125/udp\
 -p 8126:8126\
 hopsoft/graphite-statsd```
- build & run the project
```
### TODO
Put the various docker images into a docker-compose

## execute the following from project root
sbt clean run 

```

## Deployment
- local deployment
```
docker run \ 
   -p9000:9000 \
   -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
   -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} \
   -e KINESIS_STREAM_NAME=${KINESIS_STREAM_NAME} \
   -e JDBC_URL=${JDBC_URL} \
   -e JDBC_PASSWORD=${JDBC_PASSWORD} \
   -e JDBC_DIRVER=${JDBC_DIRVER} 
```

### Sample Json event
```
{
  "entity_type": "USER",
  "entity_id": "USUV71400762",
  "user_id": "26985937",
  "action": "UNLIKE"
}
```

### Table structure
```
mysql> describe like_events;
+-------------|--------------|------|-----|-------------------|-------+
| Field       | Type         | Null | Key | Default           | Extra |
+-------------|--------------|------|-----|-------------------|-------+
| id          | varchar(40)  | YES  | MUL | NULL              |       |
| user_id     | varchar(40)  | YES  | MUL | NULL              |       |
| entity_id   | varchar(140) | YES  | MUL | NULL              |       |
| entity_type | varchar(20)  | YES  | MUL | NULL              |       |
| ts          | timestamp    | NO   | MUL | CURRENT_TIMESTAMP |       |
| created_at  | timestamp    | NO   |     | CURRENT_TIMESTAMP |       |
+-------------|--------------|------|-----|-------------------|-------+
```

## Authors
* **Kayvan Kazeminejad** - *Intitial work*

### References
- [scalaz](https://github.com/scalaz)
- [doobie](https://github.com/tpolecat/doobie)
- [circe](https://github.com/circe)
- [http4s](https://github.com/http4s/http4s)
- [cats](http://typelevel.org/cats/)
- [kcl](http://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html)
