#!/bin/sh

PROJECT_ROOT=/Users/trevorallred/projects/tantalim/scala
TARGET_LIB=/Users/trevorallred/projects/tantalim/ide

SBT="./activator/activator"

DIVIDER="-------------------------"


cd ${PROJECT_ROOT}
echo ${DIVIDER}
echo "Updating Tantalim Client Libraries"
public/update_tantalim_client.sh

echo ${DIVIDER}
echo "Cleaning"
$SBT clean

echo ${DIVIDER}
echo "Packaging"
$SBT dist

echo ${DIVIDER}
echo "Unzipping Libraries"
cd ${PROJECT_ROOT}/target/universal/
rm -Rf tantalim-1.0
unzip tantalim-1.0.zip

echo ${DIVIDER}
echo "Copying Libraries"
rm -Rf ${TARGET_LIB}/lib
cp -R tantalim-1.0/lib ${TARGET_LIB}/

echo ${DIVIDER}
echo "Removing Select Libraries"
rm ${TARGET_LIB}/lib/ch.qos.logback.logback-classic-1.1.1.jar
