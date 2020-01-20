#!/bin/bash

export PRJ="$(cd `dirname $0`; pwd)"

JAR=`ls -1 $PRJ/target/wnro-patcher-*.jar | tail -1`

$JAVA_HOME/bin/java -jar $JAR $@
