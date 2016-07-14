#!/bin/bash
#
# Build given project from given branch
# Perform the following actions:
# - Checkout latest commit for project XX from branch YY (or build from working tree)
# - Build it
# - Build site
# - Upload site

set -e

if [ "$#" -ne 3 ]; then
    echo "syntax: build-release project-path branch profile"
    echo 'branch can be -- if working tree is used'
    exit 1
fi

PROJECT=$1 # relative path to project
BRANCH=$2 # branch of the project can be -- that stands for working tree
PROFILE=$3 # maven profile
FIJI="../Fiji.app.test/plugins" # fiji location (for uploading to repo)

echo 'Before continuing changelog at src/changes'
echo 'must be modified in respect to fixed bugs'
echo "Commit format: git tag -a \"SNAPSHOT-13-07-16\" -m \"Releasing snaphots to Fiji internal update site\"" 
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac

# go into project dir
cd $PROJECT
if [ "$2" == '--' ]; then
	echo You selected build from working directory
else
	echo You selected build from branch $BRANCH
	if [ -n "$(git status --porcelain)" ]; then
		echo 'Worknig directory is not clean.'
		echo 'Commit all changes first (especially changelog)'
		exit 1
	fi
	# checkout branch
	git fetch
	git checkout $BRANCH
	git pull
fi

# build project - it should be full jar
mvn clean package site -P $PROFILE
# copy artefact to Fiij
find $FIJI -name QuimP*.jar | xargs rm -fv # delete old one
cp -v target/QuimP_-*-jar-*.jar $FIJI # copy package
# Copy site
rsync -az -e 'ssh -p2222' --delete --stats \
		target/site/ \
		trac@trac-wsbc.linkpc.net:/var/www/restricted/QuimP_

echo '------------------------------------------------------------------'
echo Postprocessing:
echo Start Fiji from $FIJI and push plugin to plugin repository
if [ "$2" == "--" ]; then
	echo "Commit changes and tag them."
	echo "git tag -a \"SNAPSHOT-13-07-16\" -m \"Releasing snaphots to Fiji internal update site\""
fi


