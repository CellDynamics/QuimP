#!/bin/bash
#
# Finish hotfix branch
# Perform the following actions:
# - Remove suffix and make commit in hotfix
# - Merge with master
# - Tag master and sign
# - Merge hotfix with develop
# - Change version for development

set -e

if [ "$#" -ne 2 ]; then
    echo "syntax: finish-hotfix developmentVersion"
    echo "developmentVersion should be current vesrion in develop branch"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

developmentVersion=$1

currentVer=$(mvn help:evaluate -Dexpression=project.version | sed '4q;d' | awk '{print $4}')
# remove -SNAPSHOT
ns=$(sed -r 's/-SNAPSHOT//' <<< $currentVer)
sed -i "0,/$currentVer/s/$currentVer/$ns/" pom.xml
git checkout master
git merge hotfix/$ns -m "Finishing hotfix $ns" -S
git tag -a $ns -m "Version $ns"
git push

git checkout hotfix/$ns
# promote pom to dev version before merging with develop
sed -i "0,/$ns/s/$ns/$developmentVersion/" pom.xml
git commit -am "Pushed pom version to $developmentVersion"
git checkout develop
git merge --no-commit --no-ff hotfix/$ns
git commit -m "Merging hotfix $ns"