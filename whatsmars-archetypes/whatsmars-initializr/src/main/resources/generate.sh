#!/bin/bash

type=${1}
groupId=${2}
artifactId=${3}
package=${4}
includeActuator=${5}


if [ -z "$type" ]; then
    echo "you must enter project type: web or dubbo"
    exit 0
fi

if [ -z "$groupId" ]; then
    echo "you must enter groupId"
    exit 0
fi

if [ -z "$artifactId" ]; then
    echo "you must enter artifactId"
    exit 0
fi

rm -rf ${artifactId}
rm -f ${artifactId}.tar

mvn archetype:generate                                  \
  -DarchetypeCatalog=internal                           \
  -DarchetypeGroupId=org.hongxi                         \
  -DarchetypeArtifactId=${type}-archetype               \
  -DarchetypeVersion=2021.4.0                          \
  -DgroupId=${groupId}                                  \
  -DartifactId=${artifactId}                            \
  -Dversion=1.0.0-SNAPSHOT                              \
  -Dpackage=${package}                                  \
  -DincludeActuator=${includeActuator}                  \
  -DinteractiveMode=false

tar -zcvf ${artifactId}.tar ${artifactId}
rm -rf ${artifactId}

time=$(date "+%Y-%m-%d %H:%M:%S")
echo "${time} create project ${artifactId} successfully" >> generate-project.log
