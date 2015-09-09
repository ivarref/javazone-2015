#!/bin/bash

mvn clean package -Drelease && ./app/my-app/target/appassembler/bin/app

