# Outline of user manual

\author p.baniukiewicz
\date 10 Jul 2016

# Configuration {#manConf}

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
    <manual.rootlocation>http://quimp.linkpc.net/manual/</manual.rootlocation>
</properties>
```

whereas exact locations for **release** and **snapshots** documents are defined in appropriate profiles
as extension to `manual.rootlocation` property:

```
<properties>
        <manual.location>${manual.rootlocation}master/QuimP_Guide.html</manual.location>
</properties>

# profile 0
<properties>
        <manual.location>${manual.rootlocation}develop/QuimP_Guide.html</manual.location>
</properties>

# profile 1
<properties>
        <manual.location>${manual.rootlocation}master/QuimP_Guide.html</manual.location>
</properties>
      
```

The snapshot version of documentation is located under **develop** folder on root URL whereas release under **master** folder.

Manual is built automatically by `updateDoc.sh` script run periodically on server.

## Site {#manSite}

Additionally, the changelog build by *maven-changes-plugin* plugin is also exposed to user. Its location is defined in *pom.xml*


```
<properties>
    <site.rootlocation>http://quimp.linkpc.net/history/</site.rootlocation>
    <site.historylocation>${site.rootlocation}changes-report.html</site.historylocation>
</properties>
```  

The content of `http://quimp.linkpc.net/history` is populated by `build-release.sh` script.