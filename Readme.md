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

The project is maintained by Maven build system and consists of several sub-projects which are related to each other.

![ProjectStructure](Doxygen_doc/maven-structure.png)

The light gray components are pure *pom* artifacts that define basis for related sub-projects. Orange is the main product and remaining yellow projects are plugins for QuimP.

The order of building should be as follows:

```bash
cd pom-quimp
mvn install
cd pom-quimp-plugin
mvn install
cd QuimP
mvn install
# build other plugins
# ...
```

Exemplary script tailored to developer system is `QuimP/testbuild.sh`. Every project exists in its own repository.

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

 