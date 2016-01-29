#!/bin/bash

## This script will push all files starting with "sample." in $PUSH_FROM

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PUSH_FROM="$DIR/../library/src/test/resources"
PUSH_TO=/mnt/sdcard

for f in $PUSH_FROM/sample.*; do
	# http://stackoverflow.com/a/20533347/1275092
	size=$(numfmt --to=iec-i --suffix=B --format="%3f" $(wc -c "$f" | awk '{ print $1 }'))
	device_loc="$PUSH_TO/$(basename $f)"
	echo "Pushing $(readlink -e $f) ($size) to $device_loc"

	adb push "$f" "$device_loc"
done
