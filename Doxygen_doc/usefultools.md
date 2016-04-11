# SnakePlugin {#UsefulTools}

\author p.baniukiewicz
\date 19 Feb 2016
\tableofcontents

# Change logger configuration globally {#loggerconfig}

This script can go through all logger configuration files in all sub-directories and change logging
level

```shell
#!/bin/bash

# Set log level to all files in current directory and subdirectories that are in
# */src/ directory and have *4j2.xml pattern

# Called without parameters lists current log levels, called with one parameters replace 
# root level to thi parameter e.g. : setloglevel.sh trace
if [ "$#" -ne 1 ]; then
    find ./ -type f -name *4j2.xml -path '*/src/*' | xargs grep --color 'Root level=.*'
    exit 0
fi

level="$1"
text="Root level=\"$level\""

find ./ -type f -name *4j2.xml -path '*/src/*' | xargs sed -i -e "s/Root level=\".*\"/$text/g"
```

# Build snapshots {#bsnap}

This script builds snapshots and upload them to server packed into *zip* package. It checks also if there
is the same package on server already.

```bash
#!/bin/bash

# This script builds snapshot project from develop branches across all repositories
# It needs local copy of project repos with one tracking branch 'develop'
# To make local copies of project repos they must be cloned from master repository
# Currently it is assumed that master repo is on local computer
#
# git clone -b develop --single-branch /home/baniuk/Documents/Repos/HatSnakeFilter_quimp
# .....
# .....

# WARNING - depends of file name format for jars
# Next script compiles everything and zip using time and version dependend name
# This file is uploaded to UPLOAD_DIR but only if there is no the same file already
# Because name is different always script use hash of text file with versions to compare

# On serverside must exist file hashlist and this file must be related to diectory content
# (file may be empty on beginig) It can not be edited outside

WORKING_DIR='/home/baniuk/tmp'
TMP_DIR='/tmp/bin'
UPLOAD_DIR='trac@trac-wsbc.linkpc.net:/var/www/restricted/SNAPSHOTS'
# http://stackoverflow.com/questions/4381618/exit-a-script-on-error
mabort()
{
    echo >&2 '
    ***************
    *** ABORTED ***
    ***************
    '
    echo "An error occurred. Exiting..." >&2
    exit 1
}
cd "$WORKING_DIR"
# Assume that there are only repos
# Go through and pull changes
for d in */ ; do
    cd "$d" # go to repo
    git checkout develop &>/dev/null
    git pull # update repo
    cd ../
done

# install latest poms
cd pom-quimp
mvn install
cd ../
cd pom-quimp-plugin
mvn install
cd ../
# compile everything
cd QuimP
mvn clean install # must be installed for filters
if [ $? -ne 0 ]; then
    exit 1
fi
mvn package -P uber -Dmaven.test.skip=true # produce artefact for IJ
cd ../HatSnakeFilter_quimp
mvn package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../HedgehogSnakeFilter_quimp
mvn package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../MeanSnakeFilter_quimp
mvn package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../SetHeadSnakeFilter_quimp
mvn package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../
# collect artefacts and get their versions
rm -rf $TMP_DIR
mkdir $TMP_DIR
cp HatSnakeFilter_quimp/target/*.jar $TMP_DIR
cp HedgehogSnakeFilter_quimp/target/*.jar $TMP_DIR
cp MeanSnakeFilter_quimp/target/*.jar $TMP_DIR
cp SetHeadSnakeFilter_quimp/target/*.jar $TMP_DIR
cp QuimP/target/*-jar*.jar $TMP_DIR
# create file with repo information
touch $TMP_DIR/versions.txt
# go through repos and read last comit from develop
for d in */ ; do
    cd "$d"
    echo "$d " >> $TMP_DIR/versions.txt
    git log -1 --pretty="format:%H %an %aD %s%n" >> $TMP_DIR/versions.txt
    cd ../
done

# Get general snapshot version from quimp name
version=$(find QuimP/target -maxdepth 1 -name 'QuimP*with*.jar' | awk -F"-" '{ print $2 }')
# Get current date
cd=$(date +"%Y-%m-%dT%H%M%S")
# check if this snapshot is in our dir
# can not be done basing on filename because of time suffix
# we will calculate hash of version.txt file
h=$(sha256sum $TMP_DIR/versions.txt | awk '{ print $1 }')
# find in hashlist
# download it from remote first
scp $UPLOAD_DIR/hashlist ./
grep $h hashlist
# check status
if [ $? -eq 1 ]; then
    echo $h >> hashlist
    #create zip
    zip -j9 QuimP_$version-$cd.zip $TMP_DIR/*
    scp QuimP_$version-$cd.zip hashlist $UPLOAD_DIR/
    rm -f hashlist QuimP_$version-$cd.zip
else
    echo "File exists - do nothing"
    rm -f hashlist
fi    

echo >&2 '
************
*** DONE *** 
************
'

```  

# Generate documentation {#gendoc}

This script builds docuementation

```sh
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
```