#!/bin/bash
mvn install -B
STATUS = $?
if [ $STATUS -eq 0 ]; then
	exit 1;
else
	echo 0;
fi