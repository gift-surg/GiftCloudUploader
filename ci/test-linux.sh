#!/bin/bash
mvn install -B
if [ $? -eq 0 ]; then
	exit 0;
else
	echo 1;
fi