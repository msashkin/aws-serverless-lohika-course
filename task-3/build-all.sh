#!/usr/bin/env bash

# install locally api-dynamodb-service to share its classes with other modules
mvn clean install --file ./api-dynamodb-service/pom.xml
mvn clean package --file ./s3-sqs-service/pom.xml
mvn clean package --file ./sqs-dynamodb-service/pom.xml
mvn clean package --file ./aws-cdk-deployment/pom.xml
