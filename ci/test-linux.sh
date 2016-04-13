#!/bin/bash
echo $PWD > ~/runner_start.txt

mvn install -B
if [ $? -eq 0 ]; then
	touch ~/runner_ok.txt 
	exit 0;
else
	touch ~/runner_fail.txt 
	echo 1;
fi