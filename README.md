# Javazone 2015

## Requirements

Maven 3.3.3

## Usage

Run `./demo.sh` and you should see an output like the following:

    App is located in my-app-v2-1-g016ee92.jar
    Lib is located in lib-a-v2-1-g016ee92.jar

Re-runs (once the extension is installed in the local repo) can be done like this

    cd multi-project
    mvn clean package && ./app/my-app/target/appassembler/bin/app

Comments to refsdal.ivar@gmail.com


