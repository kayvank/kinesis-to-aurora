kinesis-to-aurora
====
 
This is an Idempotent service that persists likes/unlikes user-events from kinesis stream & persisting them in Aurora RDS.  User envents are :
- likes: storedas like events against an asset 
- unlikes: cause deletion of a previously stored like event of an asset

## Getting 
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
#### setup
```
git clone https://github.com/kayvank/user-likes-svc
sbt clean compile  // to build prject
sbt test // unit test
sbt clean compile universal:packageBin // to generate an executable
sbt clean compile docker:publishLocal
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

### References
- [doobie](https://github.com/tpolecat/doobie)
- [circe](https://github.com/circe)
- [http4s](https://github.com/http4s/http4s)
- [cats](http://typelevel.org/cats/)
- [kcl](http://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html)
