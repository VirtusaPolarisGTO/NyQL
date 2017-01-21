#!/usr/bin/env bash

mvn clean install
cd server
mvn assembly:assembly
cd ..
docker build -t nyserver-local .