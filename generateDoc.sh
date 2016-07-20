#!/bin/bash
# This script generates Doxygen doc based on java source files
# Outputs doxygen documentation using doxyfile available at Doxygen_doc
# 
# Uploads to local trac and public quimp.linkpc.net

echo "Use latest Doxygen compiled from sources to have links evaluated correctly"
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
    echo "Error - can not checkout wiki pages"
    exit 1
fi
cd QuimP.wiki
# replace syntax [link][(adderess) to [word](address.md)
perl -p -i -e 's/(\[.*\])(\((\w+)\))/$1($3.md)/g' *.md 
rsync -c --exclude "_*"  ./*.md $CURRENT_DIR
cd $CURRENT_DIR
rm -rf /tmp/QuimP.wiki

doxygen Doxyfile >/dev/null
rsync -az -e 'ssh -p2222' --stats --delete Doxygen_doc html/ trac@trac-wsbc.linkpc.net:/var/www/restricted/Doxygen_doc/QuimP
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222 -o 'IdentitiesOnly yes'" --delete --stats Doxygen_doc html/ pi@quimp.linkpc.net:/var/www/restricted/Doxygen_doc


