#!/usr/bin/env bash

mvn clean package -DskipTests
cf push moviefun -p target/moviefun.war

