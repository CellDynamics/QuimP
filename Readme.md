[![Maven Central](https://img.shields.io/maven-central/v/com.github.celldynamics.quimp/QuimP_.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.celldynamics.quimp%22%20AND%20a%3A%22QuimP_%22)

# About

QuimP software, a set of plugins for ImageJ, has been developed by Till Bretschneider and Richard 
Tyson to quantify spatio-temporal patterns of fluorescently labeled proteins in the cortex of moving 
cells.

QuimP was first described in Dormann et al., 2002. For information on the classic version, now 
called QuimP1, please follow this link.

QuimP2 which was developed in collaboration with Leonard Bosgraaf (Bosgraaf et al., 2009) 
introduced a new method to correlate local cortical fluorescence with membrane movement. An 
obsolete QuimP2 installation package can be downloaded here (Unzip the archive and move the contents
of the two folders according to their directory name. The help icon that appears when launching the
QuimP toolbar provides an in depth explanation of how to use the associated plugins and the 
individual parameters). An addon for pseudopod analysis resulted in QuimP3 which however is not 
officially supported by the main QuimP development team (contact Leonard Bosgraaf for help instead).

QuimP3 is currently under have development. It will support user plugins, other segmentation 
algorithms, image pre-processing and many other features.

# Build

Clone the repository and update submodules which are required to run tests:

```bash
git clone https://github.com/CellDynamics/QuimP.git
git submodule init
git submodule update
```

Then, use standard Maven approach, e.g.:

```bash
mvn package
```

This will produce `QuimP_xxx.jar` with program as well as separate jars with source and tests. Note that tests depend on git submodule repository which checks out to nonstandard resource folder.

## Javadoc

JavaDoc jar is not built by default. To get it use:

```bash
mvn clean com.github.jeluard:plantuml-maven-plugin:generate javadoc:jar 
```

One can also use the profile `build-javadoc` provided by *pom-scijava-base* that lays under the basis of IJ. That profile builds full Java doc with all direct dependencies attached:

```bash
mvn clean package -P build-javadoc -Dproject.build.sourceEncoding=cp1252
# QuimP uses UTF-8 encoding by default but some dependencies use other and build fails. Setting cp1252 partially solves the problem.  
```

## Maven profiles

There are the following profiles defined in QuimP pom and closely related parent poms:

1. *installation* - **default** profile, sets location of manual to `master` branch
2. *testing* - block registration window
3. *development* - sets location of manual to `develop` branch. Use also mocked BOA filters and blocks registration window.
4. *dev-collectdeps* - copies all project dependencies to `target/dependencies`
 

 
