#!/bin/bash
# builds everything without tests and populate package into test fiji


mvn -T 1C clean package -Dmaven.test.skip=true

if [[ $? -ne 0 ]] ; then
   echo Error!!!
   exit 1
fi

# delete old quimp from test fiji

find ../Fiji.app.test/plugins -name Quim[Pp]_plugin*.jar | xargs rm -fv

# copy package
cp -v QuimP/target/QuimP-*-jar-*.jar ../Fiji.app.test/plugins
