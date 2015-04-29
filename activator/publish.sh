#!/bin/sh

PROJECT_ROOT=/Users/trevorallred/projects/tantalim/scala

cd ${PROJECT_ROOT}
sbt clean
sbt dist

TARGET_LIB=/Users/trevorallred/projects/tantalim/ide/lib

ARTIFACT_VERSION=1.0

modules=(artifacts core database filterCompiler nodes scriptCompiler tantalimModels tantalimServer util)

for i in "${modules[@]}"
do
   :
   ARTIFACT_FILE=${PROJECT_ROOT}/modules/${i}/target/scala-2.11/${i}_2.11-1.0.jar
   cp ${ARTIFACT_FILE} ${TARGET_LIB}/
done

#ARTIFACT_ID=tantalim-util
#ARTIFACT_ID=tantalim-server
#ARTIFACT_FILE=${PROJECT_ROOT}/target/scala-2.11/tantalim_2.11-1.0.jar
#
#mvn install:install-file -DgroupId=com.tantalim -DartifactId=${ARTIFACT_ID} -Dversion=${ARTIFACT_VERSION} \
#    -Dpackaging=jar -Dfile=${ARTIFACT_FILE} -DlocalRepositoryPath=${REPO_ROOT}
