#!/bin/bash
mvn install -B -P Applet,Application,Webstart
if [ $? -eq 0 ]; then
	exit 0;
else
	exit 1;
fi