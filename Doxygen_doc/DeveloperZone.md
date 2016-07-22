# Developer zone

## General information

The process of building is controlled by *pom.xml* that performs the following actions:

1. Modifies the property file *quimpconfig.properties* setting there locations for user manual and changelog available from the quimP menus.
2. Generate full site with statistics and changelog - this goal is related to local Trac instance and may not work when it is not available.
3. *pom.xml* supports profiles:
  * development - set user manual to *snapshot* and use mocked filters
  * installation - produce clean jar without dependencies, user manual points to *release*
  * uber-release - produce jar with dependencies and manual points to *release*
  * uber-snapshot - produce jar with dependencies and manual points to *snapshot*

The order of building should be as follows (assuming that user checked out already all parent poms):

```bash
cd pom-quimp
mvn clean install
cd pom-quimp-plugin
mvn clean install
cd QuimP
mvn clean package -P uber-release
# build other plugins
# When QuimP changes plugins will not notify this change thus mvn clean is recommended
```

Relations among projects are depiced below.

![ProjectStructure](maven-structure.png)

It is recommended to use `build-release.sh` script for producing either *release* or *snapshot* package.

## Documentation

The QuimP uses [Doxygen](http://www.stack.nl/~dimitri/doxygen/) tool as main developer documentation builder. It works under the same principles as JavaDoc, nevertheless both systems are not fully compatible. Doxygen significantly extends JavaDoc by offering nice features like e.g. UML diagrams or caller/callee diagrams. Current QuimP developer documentation is built using both systems (JavaDoc by Maven, Doxygen doc by external script *generateDoc.sh*) but in JavaDoc one can find many unsupported tags that have not been processed correctly. These documentations are available in locations:

* [Doxygen](http://quimp.linkpc.net/html/doxygen/index.html) - includes also this wiki pages
* [JavaDoc](http://quimp.linkpc.net/html/apidocs/)   

To build documentation from the source code the following tools are necessary:

1. [plantuml.jar](http://plantuml.com/) file should be placed in `Doxygen_doc/` folder
2. [Graphviz](www.graphviz.org/) package should be available on system path
3. [mscgen](http://www.mcternan.me.uk/mscgen/) tool should be available on system path

To have links solved correctly, build should be started from *Doxygen_doc* directory:

```bash
cd Doxygen_doc
doxygen Doxyfile >/dev/null
```

## Description of QuimP structure

*Most of these links should be accessed from [Doxygen documentation](http://quimp.linkpc.net/html/doxygen/index.html) because of specific features used for documenting the source code that are not supported by GitHub wiki*

* [BOA Plugin](BOA.md) - How QuimP works - free notes about
* [Snake Plugin Interface](SnakePlugin.md) - Description of plugin interface
* [Handling configuration](ConfigurationHandling.md) - Saving and loading various configurations
* [Binary segmentation](BinarySeg.md) - About segmentation directly from black-white masks

### Development environment

* [Useful Tools](UsefulTools.md) - Exemplary scripts that may be useful during development
* [Logging](Logging.md) - Some about logging
* [User manual](Manual.md) - Description of relations between QuimP project an user manual project
* [Exceptions in code](Exceptions.md) - How to use them

