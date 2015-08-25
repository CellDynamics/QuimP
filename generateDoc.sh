#!/bin/sh
# Outputs doxygen documentation using doxyfile avaiable at Doxygen_doc
cd Doxygen_doc
doxygen Doxyfile >/dev/null
echo
echo Check Doxygen_doc subfolders for generated files 
