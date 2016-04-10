#!/bin/sh

# You can set JAVA_HOME to point to your JVM

JAVA_CMD=java
if [ ! -z "$JAVA_HOME" ] ; then
  JAVA_CMD=$JAVA_HOME/bin/java
fi
echo "$JAVA_CMD" -cp lib/myant.jar ant.StartupScript lib .
"$JAVA_CMD" -cp lib/myant.jar ant.StartupScript lib .
