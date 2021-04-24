#!/usr/bin/env bash

# install locally api-dynamodb-service to share its classes with other modules
mvn clean install --file services/api-dynamodb-service/pom.xml
mvn clean package --file services/s3-sqs-service/pom.xml
mvn clean package --file services/sqs-dynamodb-service/pom.xml
