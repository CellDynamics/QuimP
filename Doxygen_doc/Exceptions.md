# Exceptions

\author p.baniukiewicz
\date 10 Jul 2016

# Good practices

In general use logging to report exceptions to user in the following way:


```java
try {
    if (isPluginError)
        sH.storeLiveSnake(f); // so store original livesnake after segmentation
        } catch (BoaException be) {
            BOA_.log("Could not store new snake");
            LOGGER.error(be.getMessage());
            LOGGER.debug(be.getMessage(), be);
        } finally {
            imageGroup.updateOverlay(f);
            historyLogger.addEntry("Added cell", qState);
            qState.store(f); // always remember state of the BOA after modification of UI
        }
    }
```

First logger (`ERROR` or `WARN`) reports only problem description to user. But if run with less restrictive settings ([Logging](Logging.md)), it will log also full stack.

## Links

[1]. [Logging document](Logging.md)
  