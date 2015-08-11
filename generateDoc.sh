#!/bin/sh
# Outputs doxygen documentation using doxyfile avaiable at Doxygen_doc
cd Doxygen_doc
doxygen Doxyfile
echo
echo Check Doxygen_doc subfolders for generated files 
