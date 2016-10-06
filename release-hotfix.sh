#!/bin/bash
#
# Release hotfix branch
# Perform the following actions:
# - Use maven to build software
# - Upload full site to quimp.linkpc.net (restricted to logged users)
# - Upload only changes to public site
# - Upload only javadoc to public site
# - Build Doxygen documenatation for user - without source code and upload it
# - End current hotfix branch with git flow

set -e

if [ "$#" -ne 3 ]; then
    echo "syntax: build-release previousVersion releaseVersion developmentVersion"
    echo "Example: build-release.sh 16.08.01 16.08.02 16.08.03-SNAPSHOT"
    echo "Development version should be the same as in develop branch"
    echo "Previous version is the version current in pom"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

previousVersion=$1
releaseVersion=$2 
developmentVersion=$3 

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


# Create local ssh-agent
eval $(ssh-agent)
ssh-add ~/.ssh/pi
ssh-add ~/.ssh/trac

echo "Prepare changelog and commit it here"
echo '	Before continuing changelog at src/changes'
echo '	must be modified in respect to fixed bugs'
echo 'Be prepared for signing'
echo 'Hotfix branch must be naned vxx.yy.xx'.
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac

# Replace maven version
sed -i "0,/$previousVersion/s/$previousVersion/$releaseVersion/" pom.xml
# commit change
git commit -am "Pushed pom version to v$releaseVersion"

# The Maven release
mvn clean
mvn -T 1C package site -P uber-snapshot

# build documentation without source code included
./generateDoc.sh Doxyfile-no-source
# copy artefact to Fiij
find $FIJI -name QuimP*.jar ! -name QuimP_11b.jar | xargs rm -fv # delete old one except old quimp
cp -v target/QuimP_-*-jar-*.jar $FIJI # copy package
# Copy site
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/site/ pi@quimp.linkpc.net:/var/www/restricted/site
# Copy only changes for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/site/css target/site/images target/site/changes-report.html pi@quimp.linkpc.net:/var/www/html/site
# Copy only javadoc for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats target/site/apidocs/ pi@quimp.linkpc.net:/var/www/html/apidocs
# copy doxygen for users
rsync -lrtz -e "ssh -i ~/.ssh/pi -p 10222" --delete --stats Doxygen_doc/html/ pi@quimp.linkpc.net:/var/www/html/doxygen


# Clean up and finish
git flow hotfix finish -s "v$releaseVersion"

# Updating trac version
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$releaseVersion' '$d'"


echo '------------------------------------------------------------------'
echo Postprocessing:
echo Start Fiji from $FIJI and push plugin to plugin repository
echo "git push --all && git push --tags"
kill $SSH_AGENT_PID
# git push --all && git push --tags


