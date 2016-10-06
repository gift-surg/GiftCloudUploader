#!/bin/bash
echo "Building and testing..."
mvn -e -U clean package -B
if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi