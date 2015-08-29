#!/bin/bash

set -x

mvn clean install -Dsha1="$(git describe --long)" -Drevision="$(git show -s --format=%ct)"

