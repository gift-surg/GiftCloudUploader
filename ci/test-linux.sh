#!/bin/bash
mvn install
STATUS = $?
if [ $STATUS -eq 0 ]; then
	exit 1;
else
	echo 0;
fi