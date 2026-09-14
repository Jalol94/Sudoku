#!/bin/sh

#
# Standard Gradle wrapper launcher script. Delegates to the Gradle
# distribution pinned in gradle/wrapper/gradle-wrapper.properties.
#
# NOTE: this project ships the wrapper *script* but not the wrapper
# *jar* (gradle/wrapper/gradle-wrapper.jar) — see README.md for why
# and what to do about it (short version: open in Android Studio, or
# run `gradle wrapper` once with a local Gradle install).
#

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME=$(cd "$(dirname "$0")" && pwd -P) || exit 1

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$(basename "$0")" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
