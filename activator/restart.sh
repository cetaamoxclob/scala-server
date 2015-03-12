#!/bin/bash

docker stop tantalim
docker rm tantalim
docker run -d --name tantalim -p 9000:9000 -v \
    /root/slhp/scala-server/tantalim:/opt/docker/tantalim -v \
    /root/slhp/application.conf:/opt/docker/conf/application.conf tantalim/slhp
sleep 1
docker logs tantalim
sleep 5
docker logs tantalim
