#!/bin/bash
#
# Finish hotfix branch
# Perform the following actions:
# - Remove suffix and make commit in hotfix
# - Merge with master
# - Tag master and sign
# - Merge hotfix with develop
# - Change version for development
# - Build artifact and pushes it to external sites

set -e

FIJI="../Fiji.app.release/plugins" # fiji location (for uploading to repo)

if [ "$#" -ne 2 ]; then
    echo "syntax: finish-hotfix developmentVersion"
    echo "developmentVersion should be current vesrion in develop branch"
    echo "Prepare changelog"
    mvn help:evaluate -Dexpression=project.version
    exit 1
fi

echo "Prepare changelog and commit it in hotfix branch"
read -r -p "Are you sure to continue? [y/N] " response
case $response in
    [yY][eE][sS]|[yY]) 
        ;;
    *)
        exit 1
        ;;
esac
developmentVersion=$1

currentVer=$(mvn help:evaluate -Dexpression=project.version | sed '4q;d' | awk '{print $4}')
# remove -SNAPSHOT
ns=$(sed -r 's/-SNAPSHOT//' <<< $currentVer)
sed -i "0,/$currentVer/s/$currentVer/$ns/" pom.xml
sed -i "s/<tag>.*<\/tag>/<tag>v$ns<\/tag>/g" pom.xml
git commit -am "Updated pom version"
git checkout master
git merge -no-ff --no-commit hotfix/$ns
git commit -m "Finishing hotfix v$ns" -S
git tag -a v$ns -m "Version $ns"
git push

git checkout hotfix/$ns
# promote pom to dev version before merging with develop
sed -i "0,/$ns/s/$ns/$developmentVersion/" pom.xml
sed -i "s/<tag>.*<\/tag>/<tag>HEAD<\/tag>/g" pom.xml
git commit -am "Pushed pom version to $developmentVersion"
git checkout develop
git merge --no-commit --no-ff hotfix/$ns
git commit -m "Merging hotfix $ns"
git push
git branch -d hotfix/$ns


git checkout master
d=$(date +"%b %d, %Y, %H:%M:%S")
ssh trac@trac-wsbc.linkpc.net "sudo trac-admin /var/Trac/Projects/QuimP version add '$ns' '$d'"
mvn -T 1C clean package site -P uber-release
find $FIJI -name QuimP*.jar ! -name QuimP_11b.jar | xargs rm -fv # delete old one except old quimp
cp -v target/checkout/target/QuimP_-*-jar-*.jar $FIJI # copy package

# Copy site
rsync -lrtz --delete --stats target/site/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/restricted/site
# Copy only changes for users
rsync -lrtz --delete --stats target/site/css target/site/images target/site/changes-report.html admin@pilip.lnx.warwick.ac.uk:/data/www/html/site
# Copy only javadoc for users
rsync -lrtz --delete --stats target/site/apidocs/ admin@pilip.lnx.warwick.ac.uk:/data/www/html/apidocs