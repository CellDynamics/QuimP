#!/bin/sh
# This script generates Doxygen doc based on java source files
# Outputs doxygen documentation using doxyfile avaiable at Doxygen_doc
cd Doxygen_doc
doxygen Doxyfile >/dev/null
echo
echo Check Doxygen_doc subfolders for generated files 
