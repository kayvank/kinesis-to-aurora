#!/bin/ksh
##
## run docker image for local testing
##

docker run  \
       -p3306:3306 \
       -e MYSQL_ROOT_PASSWORD=${JDBC_PASSWORD} \
       -e MYSQL_DATABASE=${JDBC_DB} \
       -e MYSQL_USER=${USER} \
       -e MYSQL_PASSWORD=${JDBC_PASSWORD} \
       mysql:latest
