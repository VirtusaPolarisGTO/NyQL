#!/usr/bin/env bash

TAG_VERSION=""

if [ $# -gt 0 ]; then
    TAG_VERSION=$1
else
    echo "You need to provide docker tag in arguments!"
    exit 1
fi

mvn clean install
cd server
mvn assembly:assembly
cd ..
docker build -t nyserver-local .
docker tag nyserver-local uisuru89/nyserver:$TAG_VERSION

echo "Docker image built successfully!"
echo "-----------------------------------------------"

docker login
rc=$?

if [ ! $rc -eq 0 ]; then
    echo "Docker login failed!"
    exit $rc
fi

echo "-----------------------------------------------"
echo "Push the image using the command 'docker push uisuru89/nyserver:$TAG_VERSION'"