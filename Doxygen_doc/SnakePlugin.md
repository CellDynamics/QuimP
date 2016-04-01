
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
    user--(Load\nconfig)
    user--(Write\nconfig)
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
note right of (Handle\nconfiguration) : see Configuration Handling\ndocument
@enduml

List of use cases:

+ [Create engine](@ref ce)
+ [Run plugin](@ref rp)
+ [Select plugin](@ref sp)
+ [Show GUI](@ref sg)
+ Load config
+ Write config
+ Get plugin config
+ Set plugin config
+ [Update view](@ref upv)
 

## Description of Use Cases {#douc}

### Create Engine {#ce}

This use case is responsible for creating plugins engine. Three important operations are here.
 Firstly, the \ref uk.ac.warwick.wsbc.QuimP.PluginFactory(final Path) "PluginFactory" is 
 created which is related to scanning for plugins and registering them, secondly the QuimP UI 
 is build using discovered plugins
 (\ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildSetupPanel() "buildSetupPanel")
 and finally the list of plugins \ref uk.ac.warwick.wsbc.QuimP.SnakePluginList "SnakePluginList" 
 is created. 

The difference between PluginFactory and SnakePluginList is that PluginFactory is responsible for
searching given directory for jars and collecting information about them in its internal database. 
The SnakePluginList is strongly correlated with UI interface. It is a list of plugins that are
currently active created in dependency on user actions and on the basis of PluginFactory.
The main principle of this class is to provide mechanism of serialization that allows to save 
current plugin stack and then restore it. So this class holds whole configuration of the stack 
(where plugins are defined by their **names** obtained from PluginFactory) and allows to restore
physical instances of plugins during deserialization.   


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
:create SnakePluginList;
}
partition buildSetupPanel {
:get plugin names from ""PluginFactory"";
note left: names of all plugins\nof given type through\n**SnakePluginList** warper
:add **NONE** to this list;
note left: **NONE** has special meaning as name\nit stands for empty slot
:create ComboBox filled with names;
:get activity status from snakePluginList.isActive;
}
stop
legend
""PluginFactory"" is most important
player here. All actions related
to plugins have place there.
It is related to jars on disk
""SnakePluginList"" is related to plugin stack
end legend
@enduml

### Run Plugin {#rp}

Plugin is run on user action, when he selects plugin from selector or when he modifies parameters 
of plugin (only if plugin supports uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro interface). 
During this stage the new Snake is produced as result of applying the plugin stack on the original
snake. The original Snake is those obtained from segmentation algorithm. 
See [Technical details](@ref td) for details about Snakes handling in QuimP.

Conditions:

* User [selected](@ref sp) one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case. 
* User started segmentation or added a cell.

@startuml
start
:get ""liveSnake"";
:backup ""liveSnake"";
note left
""liveSnake"" from current ""SnakeHandler""
is stored in ""segSnakes"" for current frame
end note
partition iterateOverSnakePlugins {
:c = snake;
if (is any plugin selected) then (true)
note left: There is any instance in BOAp.sPluginList\ncreated on **Select Plugin**
while (for every Plugin)
if (if is **not** null\nif is **active**) then (true)
:attach data;
:runPlugin(**c**);
:assign result to **c**;
endif
endwhile
endif
}
:return **c**;
:storeThisSnake();
note left
Returned and processed snake is then 
stored in finalSnakes[] array
See **Update View**
end note
stop
legend
**iterateOverSnakePlugins(Snake)** is low level
method that updates Snake passed as argument.
There is other method **recalculatePlugins()**
that apply **iterateOverSnakePlugins(Snake)**
for all Snake objects on frame
end legend
@enduml

### Select Plugin {#sp}

This action is taken on plugin selection in QuimP UI. The story starts in uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.itemStateChanged(final ItemEvent)
  
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
partition SnakePluginList::setInstance {
    :build instance;
    if (IQuimpPluginSynchro) then (yes)
    :attachContext;
    note left :Attaches ViewUpdater
    endif
    :attachData;
    }
else (no)
note right
Plugins are identified by names
which are populated to JComboBoxes as well.
**NONE** is special keyword that
means deselected plugin
end note
:close plugin UI;
:delete instance;
note right: SnakePluginList
endif
}
:recalculatePlugins;
note left: Update View
stop
@enduml

### Show GUI {#sg}

This action is taken when user click **GUI** button in QuimP. Starting method: uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent)

Conditions:

* User selected one plugin on certain slot.
* Plugins have been registered already by **Create Engine** use case.
* User clicked GUI button

@startuml
start
:get instance;
note left: for selected slot from ""SnakePluginList""
if (is **not** ""null"") then (true)
:showUI(true);
note left: call interface method\nfrom plugin
endif
stop
@enduml 

There is similar activity related to GUI, namely *Activity checkbox* that switches on or off
selected plugin. The state of this checkbox is stored together with plugin in SnakePluginList.

@startuml
start
:set state;
note left: for selected slot in ""SnakePluginList""
:recalculatePlugins;
note left: Update View
stop
@enduml 

### Update View {#upv}

This use case is activated when any changes in plugin configuration is made during its activity.
The configuration may be related to internal state of plugin as well as to plugin setup in QuimP. 
All these actions may require redrawing of current screen and updating current outlines. 
For this purpose the original segmented snakes are store as well. See [technical notes](@ref td)

Conditions:

* User selected one plugin on certain slot.
* Plugins have been already registered by **Create Engine** use case.
* Plugin supports [IQuimpPluginSynchro](uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPluginSynchro) interface

Related classes and methods:

* uk.ac.warwick.wsbc.QuimP.BOA_.recalculatePlugins()

@startuml
start
if (are any snakes) then (true)
while (SnakeHandlers available?) is (yes)
    if (sH starts from current frame\nor earlier) then (yes)
    :getBacupedSnake for this frame;
    if (it is valid) then (yes)
        :iterateOverSnakePlugins;
        note left: see **Run Plugin**
        :store result as **final**;
    endif
    endif
end while
endif
stop
legend
On plugin exception, ""liveSnake"" is stored as **final**
end legend
@enduml






## General workflow for all cases {#gw}

QuimP itself creates instances of plugins as well as stores the list of active
plugins (currently 3, controlled by
\ref uk.ac.warwick.wsbc.QuimP.BOA_.NUM_SNAKE_PLUGINS "NUM_SNAKE_PLUGINS").
QuimP can also read and write options from/to plugin but it does not interfere
with these data.  
 
@startuml
actor User
participant BOA_ as QuimP
participant CustomStackWindow as GUIBuilder
participant PluginFactory
participant IQuimpPlugin as Plugin

note over GUIBuilder : Window builder
note over User : Cooperate with\nGUI by\nactionPerformed() and\n itemStateChanged()
note over Plugin : Any plugin referenced\nby instance
note over QuimP : Main class instance

== Plugin read ==
QuimP -> PluginFactory : <<create>>
note left #GreenYellow : See ""PluginFactory""
activate PluginFactory
PluginFactory -> PluginFactory : scanDirectory()
loop For every jar
    activate Plugin
    PluginFactory -> Plugin : getType()
    Plugin --> PluginFactory : //type//
    PluginFactory -> Plugin : getVersion()
    Plugin --> PluginFactory : //version//
    PluginFactory -> PluginFactory : add to ""availPlugins""
    destroy Plugin
end
PluginFactory --> QuimP

== Build GUI ==

QuimP -> GUIBuilder : buildSetupPanel()
activate GUIBuilder
GUIBuilder -> PluginFactory : getPluginNames(""type"")
PluginFactory -> PluginFactory : ""availPlugins"".get(""type"")
GUIBuilder <-- PluginFactory : //names[]//
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
""IQuimpPluginSynchro"" interface
end note
Plugin --> QuimP
QuimP -> QuimP : recalculatePlugins()
note left : GUI update

User -\ QuimP : Show GUI
QuimP -> Plugin : showUI(true)
Plugin --> QuimP

User -\ QuimP : Run Plugin
note left: Any action like\nAdd Cell or\nRun segmentation or\nUpdate view
loop over all plugins
    QuimP -> Plugin : attachData(""data"")
    Plugin --> QuimP
    QuimP -> Plugin : runPlugin()
    Plugin --> QuimP : ""data""
end    

== Action on Plugin ==
User -\ Plugin : Click in UI
Plugin -\ QuimP : updateView
QuimP -> QuimP : recalculatePlugins()

Plugin --> PluginFactory : <<Exit>>
destroy Plugin
QuimP <-- PluginFactory : <<Exit>>
destroy PluginFactory
@enduml



## Important players {#ipl}

There are three important classes here:

1. uk.ac.warwick.wsbc.QuimP.SnakePluginList - this class is related to plugin stack - three slots 
available in UI together with activity boxes. Keeps list of active plugins as Plugin objects.
Plugin objects hold reference to jar and information extracted from this jar that are serialized
SnakePluginList is responsible for creating and accessing plugins as high-level interface
2. uk.ac.warwick.wsbc.QuimP.PluginFactory - this is deliverer of plugins. It is responsible for
changing plugin names to their binary reference.
3. uk.ac.warwick.wsbc.QuimP.Serializer - this class is used during storing plugin 
stack configuration. Include \ref uk.ac.warwick.wsbc.QuimP.SnakePluginList SnakePluginList and add
extra fields related to QuimP version 

Note:

> Plugins inside QuimP are defined by their **names** that are simply names of classes. **Name** of plugin uniquely identifies its jar. All communication between all components is based on their **names**
   
The cooperation between first two modules is as follows:

@startuml
participant BOA_ as QuimP
participant Serializer as ser
participant SnakePluginList as plist
participant Plugin as plugin
participant PluginFactory as pfact
participant IQuimpPlugin as iplugin


QuimP->pfact : <<create>>
activate pfact
pfact->pfact : Scan for plugins

QuimP->plist : <<create>>()
activate plist
note left
Pass:
""NUM_SPLINE_PLUGINS""
""pluginFactory""
""viewUpdater""
endnote  
plist->plugin : <<create>> list

== Select Plugin ==
QuimP --> plist : select plugin **name**
plist->plist : setInstance
activate plist
plist->plugin : <<create>> at slot //i//
activate plugin
plugin->pfact : getInstance(**name**)
pfact->iplugin : <<create>>
activate iplugin
pfact-->plugin : ref
plist->iplugin : ref.attachContext
plist->iplugin : ref.attachData
deactivate plist
loop over plugins in SnakePluginList
    QuimP->plugin : isExecutable
    plugin-->QuimP : yes/no
    plugin-->QuimP : ref
    QuimP->iplugin : ref.attachData
    QuimP->iplugin : ref.run()
    note left
    Update view
    endnote
end

== Save config ==
QuimP->ser : <<create>>
activate ser
note left
Pass:
""quimpInfo""
""snakePluginList""
endnote  
ser->ser : save()
activate ser
ser->plist : beforeSerialize()
loop over plugins in SnakePluginList
    plist->plugin : downloadPluginConfig()
    plugin->iplugin : ref.getVersion()
    iplugin-->plugin : //version//
    plugin->iplugin : ref.getPluginConfig()
    iplugin-->plugin : //config//
end
plist-->ser
ser->ser : write

deactivate ser
destroy ser
deactivate plugin

== Load config ==
QuimP->ser : <<create>>
activate ser
QuimP->plist : closeAllWindows
loop over plugins in SnakePluginList
    plist->plist : getInstance(i)
    activate plist
    plist->iplugin : ref.showUI(false)
    deactivate plist
end
ser->ser : load()
activate ser
ser->plist : afterSerialize()
loop over plugins in SnakePluginList
    plist->plugin : get old config
    note right
    Here is instance
    loaded and created
    by GSon
    endnote
    plist->plist : setInstance(**name**,..)
    activate plist
    plist->plugin : <<create>> at slot //i//
    activate plugin
    plugin->pfact : getInstance(**name**)
    destroy iplugin
    pfact->iplugin : <<create>>
    activate iplugin
    pfact-->plugin : ref
    plist->iplugin : ref.attachContext
    plist->iplugin : ref.attachData
    deactivate plist
    plist->plugin : uploadPluginConfig()
    plugin->iplugin : ref.setPluginConfig()
end
plist-->ser
ser-->QuimP : <<new instance>>
destroy ser
@enduml












### set/get Plugin Config {#sgpc}

These use cases are important for storing and restoring plugin configuration inside QuimP configuration files. 

__This feature is not developed yet.__

@todo Add description here

# Technical details {#td}

The main storage for snakes is uk.ac.warwick.wsbc.QuimP.SnakeHandler class which contains
three important fields:

```java
    private Snake liveSnake;
    private Snake[] finalSnakes; //!< series of snakes, result of cell segm. and plugin processing
    private Snake[] segSnakes; //!< series of snakes, result of cell segmentation only
```
The `liveSnake` is initialized during object creation and it references *Snake* that is currently 
processed. There is only one `liveSnake` in every `SnakeHandler` but `SnakeHandler` may contain many *snakes* for
successive frames starting from `startFrame`. The `SnakeHandler` is created when cell is selected ([addCell](uk.ac.warwick.wsbc.QuimP.BOA_.addCell(final Roi, int)))
and in assumption it should contain all snakes resulting from segmentation of this cell in time. On the beginning
the `liveSnake` simply contains segmentation result for `startFrame`, then it is modified **in place**
by segmentation methods (mainly during [runBoa](uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int)) execution) for every next frame and stored in `finalSnakes` and `segSnakes`. These two
arrays are filled in the same manner. The `finalSnakes` contains *snakes* after processing by plugin stack
whereas the `segSnakes` contains pure *snakes* after segmentation. The `segSnakes` are snapshots of
`liveSnake` taken during segmentation process for all frames. This array is necessary for restoring 
initial state when all plugins are deselected. 

> The `liveSnake` is **not modified** by plugins  


The `finalSnakes` array holds *snakes* (segmented cells, outlines) that are considered as ready and 
stable. Content of this array is displayed on screen and saved on disk.

The most important methods that are responsible for running plugins and setting/getting data are: 

1. uk.ac.warwick.wsbc.QuimP.BOA_.addCell(final Roi, int) - run segmentation for freshly selected cell
2. uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) - run segmentation for all frames and all SnakeHandlers
3. uk.ac.warwick.wsbc.QuimP.BOA_.recalculatePlugins() - process all outlines from (`segSnakes`) by
active plugins and copy results to `finalSnakes`. This method does not run segmentation again so it 
is used for updating screen after any plugin action (inside plugin by interface `IQuimpPluginSynchro` or
by `JSpinners` in QuimP UI related to plugins)
 1. uk.ac.warwick.wsbc.QuimP.SnakeHandler.getBackupSnake(int)
 2. uk.ac.warwick.wsbc.QuimP.BOA_.iterateOverSnakePlugins(Snake)
 3. uk.ac.warwick.wsbc.QuimP.SnakeHandler.storeThisSnake(Snake, int)
4. uk.ac.warwick.wsbc.QuimP.SnakeHandler.backupLiveSnake(int) - this method makes copy of actual `liveSnake`
for frame *f* (`liveSnake` is modified for every next segmented frame). It must be called for every frame 
during segmentation before applying plugins. 

Plugins can call errors during execution. In this case the `liveSnake` is taken as final (last state 
during segmentation) 
