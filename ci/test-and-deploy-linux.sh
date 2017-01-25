#!/bin/bash
echo "Building release..."
mvn -e -U clean install site-deploy -B -P Webstart;
if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi