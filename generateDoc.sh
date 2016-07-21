#!/bin/bash
# This script generates Doxygen doc based on java source files
# Outputs doxygen documentation using doxyfile available at Doxygen_doc
# 
# Can be run with or without parameter
# If there is no parameter it is default configuration - build 
# developer doc and copy it to restriced locations. 
# If there is parameter (other doxygfile) it only builds without 
# transfering to servers
#
# Build Doxygen docymentation for project
#	- By default uses Doxyfile
#	- Can use other configuration file if provided as parameter
# Uploads to local trac and quimp.linkpc.net if no Doxyfile

DEFAULT_DOXYFILE=Doxyfile

echo "--> One must use latest Doxygen compiled from sources to have links evaluated correctly"
if [ "$#" -ne 0 ]; then
	DEFAULT_DOXYFILE=$1
	echo "--> Use provided $1 doxyfile"
	UPLOAD=0
else
	echo "--> Use default doxyfile"
	UPLOAD=1
fi

dot -Tpng Doxygen_doc/maven-structure.dot -o /tmp/maven-structure.png
# copy only if changed to prevent pushing repo
rsync -c /tmp/maven-structure.png Doxygen_doc/maven-structure.png
mkdir -p Doxygen_doc/Doxygen_doc
# for compatibility of paths between Readme.md and Readme.md embeded in Doxygen doc
cp Doxygen_doc/maven-structure.png Doxygen_doc/Doxygen_doc/maven-structure.png
cd Doxygen_doc
rm -rf html/*
CURRENT_DIR=$(pwd)

# Integrate QuimP.wiki pages from repo
cd /tmp
git clone https://github.com/CellDynamics/QuimP.wiki.git
if [ $? -ne 0 ]; then
    echo "--> Error - can not checkout wiki pages"
    exit 1
fi
cd QuimP.wiki
# replace syntax [link][(adderess) to [word](address.md)
perl -p -i -e 's/(\[.*\])(\((\w+)\))/$1($3.md)/g' *.md 
rsync -c --exclude "_*"  ./*.md $CURRENT_DIR
cd $CURRENT_DIR
rm -rf /tmp/QuimP.wiki

doxygen $DEFAULT_DOXYFILE >/dev/null
if [ $UPLOAD -eq 1 ]; then
	rsync -az -e 'ssh -p2222' --stats --delete Doxygen_doc html/ trac@trac-wsbc.linkpc.net:/var/www/restricted/Doxygen_doc/QuimP
	rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222 -o 'IdentitiesOnly yes'" --delete --stats Doxygen_doc html/ pi@quimp.linkpc.net:/var/www/restricted/Doxygen_doc
fi


