
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
keep correct naming conventions. The default type `E` usually is `Vector2d`. QuimP provides
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
    (Get plugin\nconfig)-|>(Write\nconfig):<<extend>>
    (Set plugin\nconfig)-|>(Load\nconfig):<<extend>>
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

User -\ QuimP : Show GUI
QuimP -> Plugin : showUI(true)
Plugin --> QuimP

User -\ QuimP : Run Plugin
note left: Any action like\nAdd Cell or\nRun segmentation
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
4. uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter.attachData(final List<E>)
5. uk.ac.warwick.wsbc.QuimP.plugin.snakes.IQuimpPoint2dFilter.runPlugin()

Activity diagram for use case **Run Plugin**. 

Conditions:

* User selected one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case. 
* User started segmentation or added a cell.

@startuml
start
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
:attach **c** to liveSnake;
stop
@enduml

### Select Plugin {#sp}

Related classes and methods:

1. uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent)
2. uk.ac.warwick.wsbc.QuimP.BOA_.instanceSnakePlugin(final String, int, final List<Vector2d>)
  1. uk.ac.warwick.wsbc.QuimP.PluginFactory.getInstance(final String)
  2. uk.ac.warwick.wsbc.QuimP.BOAp.sPluginList
  
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
3. uk.ac.warwick.wsbc.QuimP.BOA_.instanceSnakePlugin(final String, int, final List<Vector2d>)
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
player here. All acctions related
to plugins have place there.
end legend
@enduml

### set/get Plugin Config {#sgpc}

This use cases are important for storing and restoring plugin configuration inside QuimP configuration files. 

__This feature is not developed yet.__

@todo Add description here

