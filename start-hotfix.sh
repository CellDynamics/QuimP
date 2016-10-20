#!/bin/bash
#
# Start hotfix branch using git flow compatibile and maven release
# Perform the following actions:
# - git flow hotfix start
# - change pom version to given release with suffix -SNAPSHOT

set -e

if [ "$#" -ne 1 ]; then
    echo "syntax: start-hotfix nextReleaseVersion"
    echo ""
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

nextReleaseVersion=$1

# start hotfix branch
git flow hotfix start $nextReleaseVersion
# Update pom version to snapshot
currentVer=$(mvn help:evaluate -Dexpression=project.version | sed '4q;d' | awk '{print $4}')
sed -i "0,/$currentVer/s/$currentVer/$nextReleaseVersion-SNAPSHOT/" pom.xml
git commit -am "Pushed pom version to $nextReleaseVersion-SNAPSHOT"

