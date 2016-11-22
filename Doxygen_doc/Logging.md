# Logging

\author p.baniukiewicz
\date 10 Jul 2016

# About

By default QuimP uses _logback_ framework and logs only warnings and errors to *stderr*. To change this behaviour one can provide his own configuration file (in _xml_ format compatible with _logback_) or use one of default configurations included in QuimP package. For example to get more informative logs, run Fiji as:

```sh
./ImageJ-linux64 -Dlogback.configurationFile=quimp-logback.xml -- --java-home $JAVA_HOME
``` 
2. [Exceptions](Exceptions.md)
