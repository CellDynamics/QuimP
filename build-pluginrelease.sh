#!/bin/bash
#
# Build QuimP plugin release from current branch
# Perform the following actions:
# - Use maven release to build and create relevant commits
# - Copy artifact to test Fiji

set -e

if [ "$#" -ne 3 ]; then
    echo "syntax: build-pluginrelease releaseVersion developmentVersion pluginRoot"
    echo "Example: build-release.sh 16.08.02 16.08.03-SNAPSHOT ../plugin"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

releaseVersion=$1 
developmentVersion=$2 
pluginRoot=$3

FIJI="../Fiji.app.release/plugins" # fiji location (for uploading to repo)

currentBranch=$(git rev-parse --abbrev-ref HEAD)
filterName=$(mvn help:evaluate -Dexpression=project.name | sed '4q;d' | awk '{print $3}') 

echo "Are you in branch you want to release? ($currentBranch)"
echo "You are processing $filterName"
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac

# Start the release by creating a new release branch
git checkout -b release/$releaseVersion $currentBranch

echo "Update masterpom version to release version avalable"
echo 'Be prepared for signing'
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac
# The Maven release
mvn clean
mvn -T 1C --batch-mode release:prepare -DreleaseVersion=$releaseVersion -DdevelopmentVersion=$developmentVersion
mvn -T 1C --batch-mode release:perform

# Clean up and finish
# get back to the develop branch
git checkout $currentBranch
# merge the version back into develop
git merge --no-ff -m "Merge release/$releaseVersion into develop" release/$releaseVersion
# go to the master branch
git checkout master
# merge the version back into master but use the tagged version instead of the release/$releaseVersion HEAD
git merge --no-ff --no-commit release/$releaseVersion~1
git commit -m "Merge $releaseVersion version" -S
# Get back on the develop branch
git checkout $currentBranch

find $FIJI -name $filterName*.jar | xargs rm -fv # delete old one except old quimp
cp -v target/checkout/target/$filterName-$releaseVersion.jar $FIJI # copy package

# Updating trac version
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$releaseVersion' '$d'"
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$developmentVersion' '$d'"

echo '------------------------------------------------------------------'
echo Postprocessing:
echo Start Fiji from $FIJI and push plugin to plugin repository
echo "git push --all && git push --tags"
# git push --all && git push --tags


