#!/bin/bash
echo "Building and deploying..."
mvn -e -U clean install -B -P Webstart;
if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi