FROM dockerfile/java:oracle-java8
MAINTAINER Trevor Allred <trevor@tantalim.com>
RUN ["mkdir", "-p", "/opt/docker/logs"]
#VOLUME ["/opt/docker/logs"]
EXPOSE 9000
WORKDIR /opt/docker
USER daemon
ENTRYPOINT ["bin/tantalim"]
CMD []
USER root
ADD files /
RUN ["chown", "-R", "daemon", "."]
