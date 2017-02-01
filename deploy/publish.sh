#!/bin/bash

if [[ $TRAVIS_PULL_REQUEST == "false" ]]; then
    mvn deploy -Possrh --settings $GPG_DIR/settings.xml
    exit $?
fi