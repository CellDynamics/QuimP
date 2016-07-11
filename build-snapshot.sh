#!/bin/bash

# This script builds snapshot project from develop branches across all repositories
# It needs local copy of project repos with one tracking branch 'develop'
# To make local copies of project repos they must be cloned from master repository
# Currently it is assumed that master repo is on local computer
#
# git clone /home/baniuk/Documents/Repos/HatSnakeFilter_quimp
# .....
# .....

# WARNING - depends of file name format for jars
# Next script compiles everything and zip using time and version dependend name
# This file is uploaded to UPLOAD_DIR but only if there is no the same file already
# Because name is different always script use hash of text file with versions to compare

# On serverside must exist file hashlist and this file must be related to diectory content
# (file may be empty on beginig) It can not be edited outside

# assumes two parameters - name of branch to build snapshot from and loglevel

WORKING_DIR='/home/baniuk/tmp'
TMP_DIR='/tmp/bin'
UPLOAD_DIR='trac@trac-wsbc.linkpc.net:/var/www/restricted/SNAPSHOTS'
CWD=$(pwd)
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

if [ "$#" -ne 2 ]; then
    echo "Provide two parameters"
    echo "build-snapshot.sh branch log_level"
    exit 0
fi

BRANCH=$1
LOGLEVEL=$2

cd "$WORKING_DIR"
# Assume that there are only repos
# Go through and pull changes
for d in */ ; do
    cd "$d" # go to repo
    git checkout -- .
    git fetch --all
    git checkout $BRANCH &>/dev/null
    git pull # update repo
    # set loglevel in current dir (deprecated as QuimP, see logging.md)
    # $CWD/setloglevel.sh $LOGLEVEL
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
mvn install -P uber -Dmaven.test.skip=true # produce artefact for IJ
rm -rf QuimP-Doc/
git submodule init
git submodule update --init
cd QuimP-Doc/Docs
pdflatex QuimP_Guide.tex &>/dev/null && bibtex QuimP_Guide.aux &>/dev/null && pdflatex QuimP_Guide.tex &>/dev/null && pdflatex QuimP_Guide.tex &>/dev/null
cd ../../

cd ../HatSnakeFilter_quimp
mvn clean package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../HedgehogSnakeFilter_quimp
mvn clean package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../MeanSnakeFilter_quimp
mvn clean package
if [ $? -ne 0 ]; then
    exit 1
fi
cd ../SetHeadSnakeFilter_quimp
mvn clean package
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
cp QuimP/QuimP-Doc/Docs/QuimP_Guide.pdf $TMP_DIR
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
    #rm -f hashlist QuimP_$version-$cd.zip
else
    echo "File exists - do nothing"
    rm -f hashlist
fi    

echo >&2 '
************
*** DONE *** 
************
'
