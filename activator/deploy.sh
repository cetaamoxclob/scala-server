#!/bin/bash

APP=tantalim/slhp
TANTALIM_HOME=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../ && pwd )

cd ${TANTALIM_HOME}/activator/
echo "Running \"activator clean docker:stage\" in: `pwd`"
${TANTALIM_HOME}/activator/activator clean docker:stage
cp ${TANTALIM_HOME}/activator/Dockerfile ${TANTALIM_HOME}/target/docker/
cd ${TANTALIM_HOME}/target/docker/
echo "Building docker $APP"
docker build -t $APP .

# docker push $APP

echo docker run -d -p 9000:9000 -v ${TANTALIM_HOME}/tantalim:/opt/docker/tantalim -v ~/slhp/application.conf:/opt/docker/conf/application.prod.conf $APP
