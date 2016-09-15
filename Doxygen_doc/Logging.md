# Logging

\author p.baniukiewicz
\date 10 Jul 2016

# About

By default QuimP uses _log4j2_ framework and logs only warnings and errors to *stderr*. To change this behaviour one can provide his own configuration file (in _xml_ format compatible with _log4j2_) or use one of default configurations included in QuimP package. For example to get more informative logs, run Fiji as:

```sh
./ImageJ-linux64 -Dquimp.debugLevel=qlog4j2.xml -- --java-home $JAVA_HOME
``` 

The first part of the command, ```-Dquimp.debugLevel=qlog4j2.xml``` sets Java system property for Java runtime environment used to start Fiji. The `qlog4j2.xml` is build-in _log4j_ configuration that logs everything above _DEBUG_ to console and file located in ```${sys:java.io.tmpdir}/QuimpLogs```. Anything after separator `--` relates to ImageJ/Fiji<sup>[1](#link1)</sup>.

## Behind the scene

Every entry point to the application (like those plugins available
from *QuimP Bar* should have the following configuration hardcoded:

```java
static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
```

Basically, it loads `log4j2_default.xml` file (from classpath, available in package) when there
is no system property `quimp.debugLevel` set. Default file offers basic logging everything above 
**WARN** to console. The `quimp.debugLevel` property can be used for passing other configurations on run time:

```sh
./ImageJ-linux64 -Dquimp.debugLevel=qlog4j2.xml -- --java-home $JAVA_HOME
```      

Developers should set this property in Eclipse under run configurations either as: `-Dlog4j.configurationFile=file:src/main/resources/qlog4j2.xml` or with `quimp.debugLevel` property for every executable.

Tests may not contain that `static` part of code. For them it is enough to set _log4j_ property: `Dlog4j.configurationFile=file:src/main/resources/qlog4j2.xml`. 

The `quimp.debugLevel` property for has been introduced to avoid messing in Fiji configuration using wide understood log4j keys.
 
### Links

1. <a name="link1"></a> http://imagej.net/Java_Options
2. [Exceptions](Exceptions.md)
