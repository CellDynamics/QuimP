# Developer zone

## General information

The process of building is controlled by *pom.xml* that performs the following actions:

1. modifies property file *quimpconfig.properties* by setting there locations for user manual and changelog
2. generate full site with statistics and changelog - this is related to local Trac instance and may not work when it is not available
3. supports profiles:
  * development - set user manual to *snapshot* and use mocked filters
  * installation - produce clean jar without dependencies, user manual points to *release*
  * uber-release - produce jar with dependencies and manual points to *release*
  * uber-snapshot - produce jar with dependencies and manual points to *snapshot*

## Description of QuimP structure

* [About](Readme.md) - Readme file
* [BOA Plugin](BOA.md) - How QuimP works - free notes about
* [Snake Plugin Interface](SnakePlugin.md) - Description of plugin interface
* [Handling configuration](ConfigurationHandling.md) - Saving and loading various configurations
* [Binary segmentation](BinarySeg.md) - About segmentation directly from black-white masks

### Development environment

* [Useful Tools](UsefulTools.md) - Exemplary scripts that may be useful during development
* [Logging](Logging.md) - Some about logging
* [User manual](Manual.md) - Description of relations between QuimP project an user manual
* [Exceptions in code](Exceptions.md) - How to use them

