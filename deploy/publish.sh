#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn clean install deploy -Possrh --settings $GPG_DIR/settings.xml
    exit $?
fi