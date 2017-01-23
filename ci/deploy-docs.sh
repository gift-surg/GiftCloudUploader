#!/bin/bash
rsync -avz -e'ssh -v' --numeric-ids --delete target/site/* tmdoel@storm:/cs/sys/www0/marine/html/cmic.cs.ucl.ac.uk/giftsurg/giftclouduploader  2>&1
