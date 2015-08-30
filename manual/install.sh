#!/bin/bash

set -x

mvn versions:set -DnewVersion="$(git describe --long)"
mvn clean install 
mvn versions:revert

