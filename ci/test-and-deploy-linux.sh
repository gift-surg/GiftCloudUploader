#!/bin/bash
if [ "$CI_BUILD_REF_NAME" == "dev" ]; then
	echo "Deploying..."
	mvn -e -U clean install -B -P Webstart;
else
	echo "Packaging..."
	mvn -e -U clean package -B -P Webstart;
fi

if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi