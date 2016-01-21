
# Build {#Build}

\author p.baniukiewicz
\date 18 Jan 2016
\tableofcontents

# Build system {#buildsystem}

By calling
@code
	mvn package
@endcode
the main target is built. It appears as two separate files:
- NAME-VER.jar - without dependencies
- NAME-VER-jar-with-dependencies.jar - with dependencies but without IJ and test classes.

Versioning is currently done in `pom.xml` file and appended to target file name.
 
@todo Add common maven resources http://stackoverflow.com/questions/11454061/sharing-common-resources-between-non-jar-maven-projects
@todo deal with versioning and generating plugins.config file