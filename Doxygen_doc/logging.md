# Logging {#Logging}

\author p.baniukiewicz
\date 28 Jun 2016
\tableofcontents

See also \ref Exceptions "Exceptions document"

# About

QuimP uses Log4J2 logging framework. Every entry point to the application (like those plugins available
from *QuimP Bar* should have the following configuration programmed:

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
wars to console. The `quimp.debugLevel` can be used for passing other configurations on run time:

```sh
./ImageJ-linux64 -Dquimp.debugLevel=qlog4j2.xml -- --java-home $JAVA_HOME
```      

Developers should set this property in Eclipse run configurations either as: `-Dlog4j.configurationFile=file:src/main/resources/qlog4j2.xml` or with `quimp.debugLevel` property for every executable.

Test may not contain that `static` part of code. For them it is enough to set property read directly
by log4j: `Dlog4j.configurationFile=file:src/main/resources/qlog4j2.xml`. Own property for
executables has been introduced to avoid messing in Fiji configuration using wide understood
log4j key. 