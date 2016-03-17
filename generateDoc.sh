#!/bin/sh
# This script generates Doxygen doc based on java source files
# Outputs doxygen documentation using doxyfile avaiable at Doxygen_doc
dot -Tpng Doxygen_doc/maven-structure.dot -o /tmp/maven-structure.png
# copy only if changed to prevent pushing repo
rsync -c /tmp/maven-structure.png Doxygen_doc/maven-structure.png
mkdir -p Doxygen_doc/Doxygen_doc
# for compatibility of paths between Readme.md and Readme.md embeded in Doxygen doc
cp Doxygen_doc/maven-structure.png Doxygen_doc/Doxygen_doc/maven-structure.png
cd Doxygen_doc
rm -rf html/*
doxygen Doxyfile >/dev/null
rsync -az -e 'ssh -p2222' --stats --delete Doxygen_doc html/ trac@trac-wsbc.linkpc.net:/var/www/restricted/Doxygen_doc/QuimP
