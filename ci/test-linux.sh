#!/bin/bash
echo $PWD > ~/runner_start.txt

mvn install -B
if [ $? -eq 0 ]; then
	touch ~/runner_ok.txt 
	exit 1;
else
	touch ~/runner_fail.txt 
	echo 0;
fi