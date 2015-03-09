#!/bin/sh

APP=tantalim/slhp
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" cd ../ && pwd )

${DIR}/activator/activator clean docker:stage
cp Dockerfile target/docker/
cd target/docker
docker build -t $APP .

# docker push $APP

echo docker run -d -p 9000:9000 -v ${DIR}/tantalim:/opt/docker/tantalim -v ~/slhp/application.conf:/opt/docker/conf/application.prod.conf $APP
