#!/bin/bash
#
# Build given project from current branch
# Perform the following actions:
# - Use maven release to build and create relevant commits
# - Upload full site to quimp.linkpc.net (restricted to logged users)
# - Upload only changes to public site
# - Upload only javadoc to public site
# - Build Doxygen documenatation for user - without source code and upload it

set -e

if [ "$#" -ne 2 ]; then
    echo "syntax: build-release releaseVersion developmentVersion"
    echo "Example: build-release.sh 16.08.02 16.08.03-SNAPSHOT"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

releaseVersion=$1 
developmentVersion=$2 

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
ssh-add ~/.ssh/pi
ssh-add ~/.ssh/trac

# Start the release by creating a new release branch
git checkout -b release/$releaseVersion $currentBranch

echo "Prepare changelog and commit it here"
echo '	Before continuing changelog at src/changes'
echo '	must be modified in respect to fixed bugs'
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

# build documentation without source code included
./generateDoc.sh Doxyfile-no-source
# copy artefact to Fiij
find $FIJI -name QuimP*.jar ! -name QuimP_11b.jar | xargs rm -fv # delete old one except old quimp
cp -v target/checkout/target/QuimP_-*-jar-*.jar $FIJI # copy package
# Copy site
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/checkout/target/site/ pi@quimp.linkpc.net:/var/www/restricted/site
# Copy only changes for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/checkout/target/site/css target/checkout/target/site/images target/checkout/target/site/changes-report.html pi@quimp.linkpc.net:/var/www/html/site
# Copy only javadoc for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/checkout/target/site/apidocs/ pi@quimp.linkpc.net:/var/www/html/apidocs
# copy doxygen for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats Doxygen_doc/html/ pi@quimp.linkpc.net:/var/www/html/doxygen


# Clean up and finish
# get back to the develop branch
git checkout $currentBranch
# merge the version back into develop
git merge --no-ff -m "Merge release/$releaseVersion into develop" release/$releaseVersion
# go to the master branch
git checkout master
# merge the version back into master but use the tagged version instead of the release/$releaseVersion HEAD
git merge --no-ff -m "Merge previous version into master to avoid the increased version number" release/$releaseVersion~1
# Get back on the develop branch
git checkout $currentBranch

# Updating trac version
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$releaseVersion' '$d'"
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$developmentVersion' '$d'"

echo '------------------------------------------------------------------'
echo Postprocessing:
echo Start Fiji from $FIJI and push plugin to plugin repository
echo "git push --all && git push --tags"
kill $SSH_AGENT_PID
# git push --all && git push --tags


