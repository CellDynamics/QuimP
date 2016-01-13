#!/bin/sh
# This script generates Doxygen doc based on java source files
# Outputs doxygen documentation using doxyfile avaiable at Doxygen_doc
cd Doxygen_doc
doxygen Doxyfile >/dev/null
rsync -az -e 'ssh -p2222' --stats --delete html/ trac@trac-wsbc.linkpc.net:/var/www/restricted/Doxygen_doc/
