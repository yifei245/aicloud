#!/usr/bin/env bash
set -e
mvn -T 1C clean package -DskipTests
