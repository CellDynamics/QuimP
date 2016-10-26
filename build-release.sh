#!/bin/bash
#
# Build given project from current branch
# Perform the following actions:
# - Use maven release to build and create relevant commits
# - Upload full site to piilp (restricted to logged users)
# - Upload only changes to public site
# - Upload only javadoc to public site
# - Build Doxygen documenatation for user - without source code and upload it
# - Update pom-quimp-plugin

set -e

if [ "$#" -ne 2 ]; then
    echo "syntax: build-release releaseVersion developmentVersion"
    echo "Example: build-release.sh 16.08.02 16.08.03-SNAPSHOT"
    echo "Current version is the version listed in develop branch before any action"
    echo "Should be the same as in pom-quimp-plugin"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

releaseVersion=$1 
developmentVersion=$2 

CURRENTDIR=$(pwd)
POMPLUGINDIR="../pom-quimp-plugin/"
FIJI="../Fiji.app.release/plugins" # fiji location (for uploading to repo)

echo "Are you in branch you want to release?"
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac

currentBranch=$(git rev-parse --abbrev-ref HEAD)

# Create local ssh-agent
eval $(ssh-agent)
ssh-add ~/.ssh/quimp-backend
ssh-add ~/.ssh/trac

# Start the release by creating a new release branch
git checkout -b release/$releaseVersion $currentBranch

echo "Prepare changelog and commit it here"
echo '	Before continuing changelog at src/changes'
echo '	must be modified in respect to fixed bugs'
echo 'Be prepared for signing'
echo 'LICENSE.txt and resources/LICENSE.txt must be the same'
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

echo "One can not push to release after this point!"

# build documentation without source code included
./generateDoc.sh Doxyfile-no-source
# copy artefact to Fiij
find $FIJI -name QuimP*.jar ! -name QuimP_11b.jar | xargs rm -fv # delete old one except old quimp
cp -v target/checkout/target/QuimP_*.jar $FIJI # copy package
# Copy site
rsync -lrtz --delete --stats target/checkout/target/site/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/restricted/site
# Copy only changes for users
rsync -lrtz --delete --stats target/checkout/target/site/css target/checkout/target/site/images target/checkout/target/site/changes-report.html admin@pilip.lnx.warwick.ac.uk:/data/www/html/site
# Copy only javadoc for users
rsync -lrtz --delete --stats target/checkout/target/site/apidocs/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/apidocs
# copy doxygen for users
rsync -lrtz  --delete --stats Doxygen_doc/html/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/doxygen

# build and upload Doxygen full
./generateDoc.sh

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

# Updating trac version
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$releaseVersion' '$d'"
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$developmentVersion' '$d'"

# Updating plugins master pom
cd $POMPLUGINDIR
./pushToNewVersion.sh $releaseVersion $developmentVersion
cd $CURRENTDIR
# copy current license to web page
scp LICENSE.txt admin@pilip.lnx.warwick.ac.uk:/data/www/html

echo '------------------------------------------------------------------'
echo Postprocessing:
echo Start Fiji from $FIJI and push plugin to plugin repository
echo "git push --all && git push --tags"
kill $SSH_AGENT_PID
# git push --all && git push --tags


