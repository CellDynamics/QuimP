# SnakePlugin {#UsefulTools}

\author p.baniukiewicz
\date 19 Feb 2016
\tableofcontents

# Change logger configuration globally {#loggerconfig}

This script can go through all logger configuration files in all sub-directories and change logging
level.

**Currently it is deprecated due to feature #179!!**

File: setloglevel.sh

# Build snapshots {#bsnap}

This script builds snapshots and ~~upload them to server~~ packed into *zip* package. It checks also if there
is the same package on server already and set certain log level for every package.

File: build-snapshot.sh *branch* *profile*

**Currently it is deprecated due to switching to other distribution model**

# Build release {#bsnap1}

This script build snapshot or release and prepare relevant site with changelog, jar is copied to Fiji where it should be uploaded to plugin repo.

File build-release.sh *project-path* *branch* *profile*

# Generate documentation {#gendoc}

This script builds documentation

File: generateDoc.sh

# Prepare test environment {#testenv}

This scripts compiles everything (QuimP and plugins) without tests and copies artifacts to Fiji directory. It is 
folder dependent. Can be used for tests from Eclipse. 

File: testBuild.sh

