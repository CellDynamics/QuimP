# Outline of user manual {#Manual}

\author p.baniukiewicz
\date 10 Jul 2016
\tableofcontents

# Configuration

QuimP assumes two versions of user manual:

1. Release version that is in general last commit from *master* branch - every release is tagged but then it can be pushed forward by small patches like misspelling corrections, etc.
2. Development version - in general last commit from *develop* branch.

QuimP opens user manual on demand (when user clicks menu entry). The manual must be available on
Internet and the path is configurable by *property* file `quimpconfig.properties` and key `manualURL`.
This key is read by \ref uk.ac.warwick.wsbc.QuimP.PropertyReader "PropertyReader" class on demand.

The value of the key `manualURL` is a placeholder filled by Maven during compilation. Thus part
of manual configuration lies in `pom.xml`. The main location of documentation is given by:

```
<properties>
    <manual.rootlocation>http://homepages.warwick.ac.uk/~u1473350/</manual.rootlocation>
</properties>
```

whereas exact locations for **release** and **snapshots** documents are defined in appropriate profiles
as extension to `manual.rootlocation` property:

```
<properties>
        <manual.location>${manual.rootlocation}release/</manual.location>
</properties>


<properties>
        <manual.location>${manual.rootlocation}snapshot/</manual.location>
</properties>
```

The snapshot version of documentation is located under **snapshot** folder on root URL whereas release under **release** folder.