#!/usr/bin/env bash

cd services

cd api-dynamodb-service
sls deploy
cd ..

cd s3-sqs-service
sls deploy
cd ..

cd sqs-dynamodb-service
sls deploy
cd ..
