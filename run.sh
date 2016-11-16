#!/bin/sh
java -Xmx500m -jar target/dependency/webapp-runner.jar --port 8080 --session-store memcache --path '' target/graph-caratheodory-np3-1.0-SNAPSHOT

