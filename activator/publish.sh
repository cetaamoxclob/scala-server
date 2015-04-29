#!/bin/sh

PROJECT_ROOT=/Users/trevorallred/projects/tantalim/scala
TARGET_LIB=/Users/trevorallred/projects/tantalim/ide

SBT="./activator"

DIVIDER="-------------------------"

cd ${PROJECT_ROOT}
echo ${DIVIDER}
echo "Cleaning"
$SBT clean
echo ${DIVIDER}
echo "Packaging"
$SBT dist

echo ${DIVIDER}
echo "Copying Libraries"
cd ${PROJECT_ROOT}/target/universal/
rm -Rf tantalim-1.0
unzip tantalim-1.0.zip
rm -Rf ${TARGET_LIB}/lib
cp -R tantalim-1.0/lib ${TARGET_LIB}/
