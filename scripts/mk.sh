#!/bin/bash

## This script will create several archives which contain the files located
## in the sources/ folder.

set -e

DEBUG=false

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
RESOURCES=$(readlink -f "$DIR/../lib/src/test/resources")
FILES="sources"
FILE="$FILES"/pcgpe10.txt # Largest file (2.2mb)
filename=$(basename "$FILE")
FILE_EXT="${filename##*.}"
unset filename
JAR="$FILES"/junit.jar    # Random jar for pack200
PREFIX="sample"
OUT="$RESOURCES/$PREFIX"

if [ ! -d "$RESOURCES" ]; then
    mkdir "$RESOURCES"
fi

gzip_template="-kc -9 $FILE > $OUT.$FILE_EXT"
# Maximum compression for using gzip. See http://superuser.com/a/305141/285466
GZIP="-9"

declare -A cmds
cmds["7z"]="7z a -mx9 $OUT.7z $FILES"
cmds["bzip2"]="bzip2 $gzip_template.bz2"
cmds["gzip"]="gzip $gzip_template.gz"
cmds["jar"]="jar cf $OUT.jar sources/HelloWorld.class"
cmds["lzma"]="lzma -9 $gzip_template.lzma"
cmds["pack200"]="pack200 -g $OUT.pack $JAR"
cmds["pack200/gzip"]="pack200 $OUT.pack.gz $JAR"
cmds["rar"]="rar a -m5 $OUT.rar $FILES"
cmds["tar"]="tar cf $OUT.tar $FILES"
cmds["tar/gzip"]="tar czf $OUT.tar.gz $FILES"
cmds["tar/bzip2"]="tar cjf $OUT.tar.bz2 $FILES"
cmds["tar/xz"]="tar cJf $OUT.tar.xz $FILES"
cmds["xz"]="xz -9 $gzip_template.xz"
cmds["zip"]="zip -9 -r $OUT.zip sources"

echo "Removing $RESOURCES/sample.*"
find "$RESOURCES" -name "sample.*" -delete

# cd into resources because this will eliminate full paths being used in zip, jar, tar, etc.
cd "$RESOURCES"
for method in "${!cmds[@]}"; do
    cmd="${cmds[$method]}"
    echo "Creating $method archive..."
    if [[ "$DEBUG" == true ]]; then
        echo "  --> $cmd"
    fi
    eval "$cmd" 1>/dev/null
done
# Go back to the other directory
cd --

echo "Done. Archives can be found at $RESOURCES/"

