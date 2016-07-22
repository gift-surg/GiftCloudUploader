#!/bin/bash
mvn -e -U clean test -B -P Webstart;

if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi
