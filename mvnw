#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.2.0
# ----------------------------------------------------------------------------

set -e

# Find the project base dir
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
  MAVEN_PROJECTBASEDIR="$(cd "$(dirname "$0")" && pwd)"
fi

# Maven home
if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"
fi

# Download Maven if needed
if [ ! -f "$MAVEN_HOME" ]; then
  echo "Maven not found. Please install Maven or check your PATH."
  echo "Download from: https://maven.apache.org/download.cgi"
  exit 1
fi

exec "$MAVEN_HOME" "$@"
