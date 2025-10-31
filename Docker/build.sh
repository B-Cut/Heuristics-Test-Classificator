#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "usage: $0 <github-repo>"
    exit
fi

mkdir "target"
git clone "$1" target
cd target || exit

if ls | grep -q gradle; then
  echo "Building with gradle"
  export USE_GRADLE='1'
  ./gradlew clean build
else
  echo "Building with maven"
  export USE_GRADLE='0'
  mvn -N wrapper:wrapper
  ./mvnw dependency:resolve-plugins dependency:go-offline -N -B
  ./mvnw -DskipTests clean install
fi;

