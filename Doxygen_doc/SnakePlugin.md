
# SnakePlugin {#SnakePlugin}

\author p.baniukiewicz
\date 19 Feb 2016
\tableofcontents

# Description of use {#dou}

The snake plugin use case support user plugins for processing snakes after segmentation.
According to general QuimP workflow
@dot
digraph QuimP_w {
    rankdir=LR;
    node [shape=square]
    "Snake\nFilter" [style=filled, fillcolor=red]
    
    "Load\nImage" -> Segmentation
    Segmentation -> "Snake\nFilter"
    "Snake\nFilter" -> "Store\nOutline"
}
@enddot

snake plugins exist in the end of chain. They can process cell outlines before
they are stored in QuimP.

Plugins are separate jar files that must be available on path passed to uk.ac.warwick.wsbc.QuimP.PluginFactory(final Path)
which is the main engine for loading jars and creating their instances. Snake plugins must be
derived from uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter interface and they must
keep correct naming conventions. QuimP provides
uk.ac.warwick.wsbc.QuimP.plugin.utils.QuimpDataConverter class for converting this type to separate 
`x` and `y` arrays of coordinates.

See uk.ac.warwick.wsbc.QuimP.PluginFactory class documentation for details.

## Use case diagram {#ucd}

The use case of snake plugin is as follows:

@startuml
left to right direction

:User: as user
rectangle "Plugin Service" {
    :QuimP: as quimp
    quimp--(Create Engine)
    user--(Run plugin)
    user--(Select plugin)
    user--(Show GUI)
    quimp--(Get plugin\nconfig)
    quimp--(Set plugin\nconfig)
    quimp--(Update View)
    user--(Update View)
    (Get plugin\nconfig)<|-(Write\nconfig):<<include>>
    (Set plugin\nconfig)<|-(Load\nconfig):<<include>>
    (Write\nconfig)-|>(Handle\nconfiguration):<<extend>>
    (Load\nconfig)-|>(Handle\nconfiguration):<<extend>>
}
note bottom of user : Related to GUI actions
note top of quimp : Plugin engine
note right of (Create Engine) : Initialization state
note "On load/write QuimP configuration" as N2
(Get plugin\nconfig) .. N2
N2 .. (Set plugin\nconfig)
@enduml

## General workflow for all cases {#gw}

QuimP itself creates instances of plugins as well as stores the list of active
plugins (currently 3, controlled by
\ref uk.ac.warwick.wsbc.QuimP.BOAp.NUM_SPLINE_PLUGINS "NUM_SPLINE_PLUGINS").
QuimP can also read and write options from/to plugin but it does not interfere
with these data.  
 
@startuml
actor User
participant BOA_ as QuimP
participant CustomStackWindow as GUIBuilder
participant PluginFactory
participant IQuimpPlugin as Plugin

note over GUIBuilder : Window builder
note over User : Cooperate with\nGUI by\nactionPerformed()
note over Plugin : Any plugin referenced\nby instance
note over QuimP : Main class instance

== Plugin read ==
QuimP -> PluginFactory : <<create>>
activate PluginFactory
PluginFactory -> PluginFactory : scanDirectory()
activate PluginFactory
PluginFactory -> PluginFactory : create qname
PluginFactory -> PluginFactory : getPluginType(qname)
activate PluginFactory #Bisque
PluginFactory -> Plugin : <<create>>
activate Plugin
PluginFactory <-- Plugin
PluginFactory -> Plugin : setup()
Plugin --> PluginFactory : //type//
PluginFactory-->PluginFactory : //type//
destroy Plugin
deactivate PluginFactory
PluginFactory -> PluginFactory : fill ""availPlugins""
PluginFactory <-- PluginFactory : ""availPlugins""
deactivate PluginFactory
PluginFactory --> QuimP

== Build GUI ==

QuimP -> GUIBuilder : buildSetupPanel()
activate GUIBuilder
GUIBuilder -> PluginFactory : getPluginNames
PluginFactory -> PluginFactory : ""availPlugins"".get(""type"")
GUIBuilder <-- PluginFactory : ""String[]""
note left
Use List to create
UI controls. Names
are related to plugin
class name
end note


GUIBuilder --> QuimP
deactivate GUIBuilder

== Use ==

User -\ QuimP : Select plugin
note left #GreenYellow : See Activity diagram 1
QuimP -> PluginFactory : getInstance(name)
PluginFactory -> PluginFactory : ""availPlugins"".get(""plugin"")
PluginFactory -> Plugin : <<create>>
activate Plugin
PluginFactory <-- Plugin
PluginFactory --> QuimP : ""instance""

QuimP -> QuimP :""sPluginList"".set
QuimP -> Plugin :attachData(""data"")
note left #OrangeRed
""data"" may be ""null""
plugin must deal with it
This may happen when plugin is
selected when no snakes is yet.
end note
Plugin --> QuimP
QuimP -> Plugin :attachContext(""ViewUpdater"")
note left #aqua
Only if plugin supports
""IPluginSynchro"" interface
end note
Plugin --> QuimP

User -\ QuimP : Show GUI
QuimP -> Plugin : showUI(true)
Plugin --> QuimP

User -\ QuimP : Run Plugin
note left: Any action like\nAdd Cell or\nRun segmentation or\nUpdate view
loop BOAp.sPluginList
    QuimP -> Plugin : attachData(""data"")
    Plugin --> QuimP
    QuimP -> Plugin : runPlugin()
    Plugin --> QuimP : ""data""
end    

Plugin --> PluginFactory : <<Exit>>
destroy Plugin
QuimP <-- PluginFactory : <<Exit>>
destroy PluginFactory
@enduml

## Description of Use Cases {#douc}

### Run Plugin {#rp}

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.addCell(final Roi, int)
2. uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int)
3. uk.ac.warwick.wsbc.QuimP.BOAp.sPluginList
4. uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter.attachData(final List<Point2d>)
5. uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter.runPlugin()
6. uk.ac.warwick.wsbc.QuimP.BOA_.iterateOverSnakePlugins(final Snake)

Activity diagram for use case **Run Plugin**. 

Conditions:

* User selected one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case. 
* User started segmentation or added a cell.

@startuml
start
partition iterateOverSnakePlugins {
if (is any plugin selected) then (true)
note left: There is any instance in BOAp.sPluginList\ncreated on **Select Plugin**
:get ""liveSnake"";
:set **c**=""liveSnake"";
while (for every BOAp.sPluginList)
if (is **not** null) then (true)
:attach data;
:runPlugin(**c**);
:assign result to **c**;
endif
endwhile
endif
}
:attach **c** to liveSnake;
note left
Attaching is done locally in e.g. runBoa
or addCell. This method is also used for
updating outlines after changing plugin 
configuration. 
See **Update View**
end note
stop
@enduml

### Select Plugin {#sp}

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent)
2. uk.ac.warwick.wsbc.QuimP.BOA_.instanceSnakePlugin(final String, int, final List<Point2d>)
  1. uk.ac.warwick.wsbc.QuimP.PluginFactory.getInstance(final String)
  2. uk.ac.warwick.wsbc.QuimP.BOAp.sPluginList
  3. uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter.attachData(final List<Point2d>)
  4. uk.ac.warwick.wsbc.QuimP.plugin.IPluginSynchro.attachContext(final ViewUpdater)
  
Activity diagram for use case **Select Plugin**.
  
Conditions:

* User selected one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case.

@startuml
start
if (is any snake?) then (yes)
    :""dataToProcess""=**activesnake**;
else (no)
    :""dataToProcess""=**null**;
endif
partition instanceSnakePlugin {
if (selectedPlugin!=NONE) then (yes)
:getInstance;
if (IPluginSynchro) then (yes)
:attachContext;
note left :Attaches ViewUpdater
endif
:register instance;
note left: BOAp
:attachData;
else (no)
note right
Plugins are identified by names
which are populated to JComboBoxes as well.
**NONE** is special keyword that
means deselected plugin
end note
:close plugin UI;
:deregister instance;
note right: BOAp
endif
if (check on possible plugin error) then (yes)
:log this situation;
endif
note right
getInstance can return **null** when plugin could not be loaded or 
instanced. In general ""PluginFactory"" try to mask most of 
exceptions. Here we can detect this situation.
end note
}
stop
@enduml

### Show GUI {#sg}

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent)
2. uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin.showUI(boolean)
3. uk.ac.warwick.wsbc.QuimP.BOA_.instanceSnakePlugin(final String, int, final List<Point2d>)
  1. uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin.showUI(boolean)

Activity diagram for use case **Show GUI**.
  
Conditions:

* User selected one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case.
* User clicked GUI button

@startuml
start
:get instance;
note left: for selected slot from ""BOAp.sPluginList""
if (is **not** ""null"") then (true)
:showUI(true);
note left: call interface method\nfrom plugin
endif
stop
@enduml 

### Create Engine {#ce}

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.run(final String)
2. uk.ac.warwick.wsbc.QuimP.PluginFactory.PluginFactory(final Path)
3. uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildSetupPanel()
    1. uk.ac.warwick.wsbc.QuimP.PluginFactory.getPluginNames(int)

Conditions:

* QuimP is starting
* Plugin dir is passed to QuimP

@startuml
start
partition BOA_:run() {
:create PluginFactory;
note left 
See ""PluginFactory"" for details
end note
}
partition buildSetupPanel {
:get plugin names;
note left: names of all plugins\nof given type
:add **NONE** to this list;
note left: **NONE** has special meaning as name\nit stands for empty slot
:create ComboBox filled with names;
}
stop
legend
""PluginFactory"" is most important
player here. All actions related
to plugins have place there.
end legend
@enduml

### Update View {#upv}

This use case is activated when any changes in plugin configuration
is made during its activity. The configuration may be related to internal state of plugin as well as to plugin setup in QuimP. All these actions may require redrawing of current screen and updating current outlines.

Conditions:

* User selected one plugin on certain slot.
* Plugins have been already registered by **Create Engine** use case.
* Plugin supports [IPluginSynchro](uk.ac.warwick.wsbc.QuimP.plugin.IPluginSynchro) interface

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.recalculatePlugins()
2. uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent)

@startuml
start
if (are any snakes) then (true)
repeat
    :getLivesSnake;
    :iterateOverSnakePlugins(snake);
    note left: see **Run Plugin**
    :storeThisSnake(snake);
    note left: Store result as **final** snake
repeat while (more snakes?) is (true)
endif
stop
legend
Depending on ""BOAp.stopOnPluginError"" state on any error
called from plugin, **final** snake can be ""liveSnake"" 
(after segmentation) or nothing.
end legend
@enduml

### set/get Plugin Config {#sgpc}

These use cases are important for storing and restoring plugin configuration inside QuimP configuration files. 

__This feature is not developed yet.__

@todo Add description here

# Technical details {#td}

The main storage for snakes is uk.ac.warwick.wsbc.QuimP.SnakeHandler class which basically contains
two important fields:

```java
    private Snake liveSnake;
    private Snake[] snakes; // series of snakes
```
The `liveSnake` is initialized during object creation and it references *Snake* that is currently 
processed. Usually it means that the *snake* has been already created and can be segmented. 
Initially this *snake* is created from e.g. *Roi* as rough approximation of object's shape and then
it is processed by segmentation algorithm.

The `snakes` array holds *snakes* (segmented cells, outlines) that are considered as ready and 
stable. Content of this array is displayed on screen and saved on disk.

The plugins work on both these data. They get *snake* stored in `liveSnake` and return modified 
new *snake* that is copied to final `snakes`. Assuming that `liveSnake` always contains only 
segmented (or raw) contours, plugins can process these data and store results in final array. The
most important methods that are responsible for running plugins and setting/getting data are: 

1. uk.ac.warwick.wsbc.QuimP.BOA_.addCell(final Roi, int) - run segmentation for freshly selected cell
2. uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) - run segmentation for all frames and all SnakeHandlers
3. uk.ac.warwick.wsbc.QuimP.BOA_.recalculatePlugins() - process all outlines (`liveSnake`) by
active plugins and copy results to `snakes`. This method does not run segmentation again so it 
is used for updating screen after any plugin action (inside plugin by interface IPluginSynchro or
by JSpinners in QuimP UI related to plugins)

Plugins can call errors during execution. In this case the `BOAp.stopOnPluginError` decides what data will be stored in `snakes`. They can be original segmented only `liveSnake` or nothing. 
