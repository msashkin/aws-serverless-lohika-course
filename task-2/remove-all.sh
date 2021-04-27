#!/usr/bin/env bash

cd services

cd api-dynamodb-service
sls remove
cd ..

cd s3-sqs-service
sls remove
cd ..

cd sqs-dynamodb-service
sls remove
cd ..
