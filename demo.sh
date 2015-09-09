#!/bin/bash

mvn clean install -f versions-git-describe/pom.xml
cd multi-project
mvn clean package -Drelease && ./app/my-app/target/appassembler/bin/app

