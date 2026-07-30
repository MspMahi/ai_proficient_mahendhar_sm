#!/usr/bin/env sh
MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" "$@"
