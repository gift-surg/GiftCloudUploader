#!/bin/bash
mvn test -B
if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi