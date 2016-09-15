#!/bin/bash

# Set log level to all files in current directory and subdirectories that are in
# */src/ directory and have *4j2.xml pattern

# Called without parameters lists current log levels, called with one parameters replace 
# root level to thi parameter e.g. : setloglevel.sh trace

echo This method is no longer recomended due to separate logger configs.
exit 1

if [ "$#" -ne 1 ]; then
    find ./ -type f -name *4j2.xml -path '*/src/*' | xargs grep --color 'Root level=.*'
    exit 0
fi

level="$1"
text="Root level=\"$level\""

find ./ -type f -name *4j2.xml -path '*/src/*' | xargs sed -i -e "s/Root level=\".*\"/$text/g"
