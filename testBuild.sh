#!/bin/bash
# builds plugins without tests and populate packages into test fiji

cd .. # assume location in QuimP

# delete old stuff
find Fiji.app.test/plugins -name QuimP*.jar | xargs rm -fv
find Fiji.app.test/plugins -name *_quimp*.jar | xargs rm -fv

cd QuimP
mvn -q -T 1C clean install -P uber -Dmaven.test.skip=true
if [[ $? -ne 0 ]] ; then
   	echo Error!!!
   	exit 1
fi
# copy package
cp -v target/QuimP_-*-jar-*.jar ../Fiji.app.test/plugins
cd ..

# iterate over plugins dirs
for d in *_quimp/ ; do
    cd "$d"
    mvn -q -T 1C clean package -Dmaven.test.skip=true 
    if [[ $? -ne 0 ]] ; then
   		echo Error!!!
   		exit 1
   	fi
   	# copy artefact to Fiji
   	cp -v target/*_quimp*.jar ../Fiji.app.test/plugins
   	cd ..
done
