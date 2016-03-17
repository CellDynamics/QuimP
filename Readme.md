# About {#About}

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

# Project structure

The project is maintained by Maven build system and consists of several sub-projects:

![ProjectStructure](Doxygen_doc/maven-structure.png)

The light gray components are pure *pom* artifacts that collect related sub-projects. The blue 
projects are for testing only, orange is the main product and three yellow projects are plugins for 
QuimP.

Because of loop dependencies (plugins depend on QuimP but QuimP requires blue plugins for tests) the 
project should be build in two stages. The simplest approach is as follows:

 ```bash
 cd QuimP-plugin
 mvn clean
 # First build all projects but without tests
 # Actually we need only plugins_test
 mvn package -Dmaven.test.skip=true -am -pl Test-Plugins/plugin1_quimp/,Test-Plugins/plugin2_quimp/
 # build with tests fat jar
 mvn package -P uber
 # generate site
 mvn site
 # install QuimP to repo (no fat jar)
 mvn install
 ```

The project requires **master pom** that currently exists in separate repository and it should be installed into local Maven repo:

 ```bash
cd pom-quimp
mvn install
 ```

_Due to issues with `Cobertura` project can not be build calling `mvn package site`_

 For QuimP two separate files are built:
- *NAME-VER.jar* - without dependencies
- *NAME-VER-jar-with-dependencies.jar* - with dependencies but without IJ and test classes.
 
# Documentation

*Doxygen* is used as main documentation tool. To render Doxygen documentation correctly, 
the following tools are necessary:

1. [plantuml.jar](http://plantuml.com/) file should be placed in `Doxygen_doc/` folder
2. [Graphviz](www.graphviz.org/) package should be available on system path
3. [mscgen](http://www.mcternan.me.uk/mscgen/) tool should be available on system path

To render documentation use `generatedoc.sh` script (currently tailored to developer system) 
or simply type:

```bash
dot -Tpng Doxygen_doc/maven-structure.dot -o Doxygen_doc/maven-structure.png
cd Doxygen_doc
doxygen Doxyfile >/dev/null
```

 