# Configuration Handling

\author p.baniukiewicz
\date 1 Apr 2016

# General file operations {#genfile}
File names are defined in configuration class \ref uk.ac.warwick.wsbc.QuimP.BOAState.BOAp "BOAp" (see comments in Doxygen doc). The very general workflow for saving and loading configurations for particular parts of QuimP is presented below.

## Data structures {#datastructures}
Currently data structures in BOA and the whole QuimP are not unified. The main class is uk.ac.warwick.wsbc.QuimP.BOAState.BOAp that keeps internal parameters as well as external (exposed to user). The latter are hold in sub-class uk.ac.warwick.wsbc.QuimP.BOAState.SegParam. The object of \ref uk.ac.warwick.wsbc.QuimP.BOAState.BOAp "BOAp" is made `static` and accessed directly from various QuimP classes (e.g. Snake). Additionally there is helper class \ref uk.ac.warwick.wsbc.QuimP.BOAState that keeps all parameters related to BOA state. Due to complicated relations \ref uk.ac.warwick.wsbc.QuimP.BOAState.SegParam "SegParam" object is referenced in this class but it is created outside it. This class is also serialized what supports saving the whole state of BOA.  

@startuml
BOAp *-- SegParam
BOAState o-- SegParam
IQuimpSerialize <|.. BOAState
BOA *-- BOAp

BOAp : +SegParam
SegParam : +nodeList
SegParam : +f_image
SegParam : +equals()
SegParam : +hashCode()
SegParam : +setDefaults()
SegParam : ...()
SegParam : ...

BOAState : +SegParam
BOAState : +SnakePluginList
BOAState : +Nest
BOAState : ...
BOAState : + beforeSerialize()
BOAState : + afterSerialize()

class BOA{
{static} BOAp boap
}
@enduml

@startuml
participant BOA
participant BOAp
participant SegParam
participant BOAState
participant SnakePluginList

-> BOA : <<create>>
activate BOA
BOA->BOAp : <<create>>
activate BOAp
BOAp -> SegParam : <<create>>
activate SegParam
SegParam --> BOAp : //segParam//
-> BOA : run()
BOA -> BOAState : <<create>>
activate BOAState
BOAp --> BOAState : assign //segParam//
BOA -> SnakePluginList : <<create>>
activate SnakePluginList
SnakePluginList --> BOAState : //snakePluginList//
BOA -> BOA : setup()
activate BOA
BOA -> Nest : <<create>>
activate Nest
Nest --> BOAState : //nest//
deactivate BOA
@enduml

The full state of BOA contains:
1. Status of plugin stack
2. Segmentation parameters
3. Current snakes

All these data are stored in BOAState object for every frame separately (every class has its own
copy constructor) as copies.

## BOA {#fileboa}
BOA produces several files:
1. *paQP* - main configuration file, contains segmentation parameters 
2. *snQP* - contains Snake objects for every frame, separate file for every Snake
3. *stQP* - statistic file
4. **pgQP** - new configuration file, holds plugin parameters (see @ref genfile)
5. **QCONF** - new configuration file, contains all Snakes for all frames (dump of \ref uk.ac.warwick.wsbc.QuimP.BOAState "BOAState") and all configuration related to segmentation

Files **paQP** and **snQP** and **stQP** are saved for every tracked Snake separately with suffixes indicating Snake ID number.

@startuml
Actor User
participant "BOA::Finish()"
participant Nest
participant SnakeHandler
participant BOAp
participant Snake
participant QParams
participant Serializer

User-->"BOA::Finish()"
note left: Click Quit button
"BOA::Finish()" -> Nest: writeSnakes()
loop over all SnakeHandlers
Nest -> Nest : get SnakeHandler
Nest -> SnakeHandler : setEndFrame()
Nest -> SnakeHandler : writeSnakes()
note left #aqua
Produce //snQP// file
separate for every ""Snake""
---
Produce //paQP// file
separate for every ""Snake""
end note
SnakeHandler -> BOAp : get file names
BOAp -->SnakeHandler
loop over all Snakes in SnakeHandler
SnakeHandler -> SnakeHandler : getStoredSnake()
SnakeHandler -> Snake : setPositions()
SnakeHandler -> SnakeHandler : write()
note left #aqua
Add Snake for every
frame to //snQP// file
end note
end
SnakeHandler -> BOAp : writeParams()
note left #red : Write //paQP// file\nseparate for every\nwritten snake
BOAp -> QParams : <<create>>
activate QParams
BOAp-->QParams : //set all fields//
QParams -> QParams : writeParams()
QParams --> BOAp
destroy QParams
end
"BOA::Finish()" -> Nest: analyse()
note left #red : Write //stQP// file\nsee **note**
"BOA::Finish()" -> Serializer : <SnakePluginList>
Serializer -> Serializer : save()
note left #aqua : Write //pgQP// file
"BOA::Finish()" -> Serializer : <BOAState>
Serializer -> Serializer : save()
note left #orange : Write //QCONF// file
@enduml

Method \ref uk.ac.warwick.wsbc.QuimP.Nest.analyse(ImagePlus) is more complicated. It uses original ImagePlus object to perform some statistical calculations based on selected Outlines. Moreover it shares parameters and Snakes through **file** (Outline and QParam classes). From `8d20cee` this method gets **copy** of original image because it set slice in stack (\ref uk.ac.warwick.wsbc.QuimP.CellStat.record()).

## Description of use for SnakePluginList {#douconfig}

SnakePluginList stores configuration data for plugin stack and every active plugin. The scheme below shows general usage of \ref uk.ac.warwick.wsbc.QuimP.Serializer "Serializer" class.

@startuml
participant BOA_ as QuimP
participant Serializer as ser
participant SnakePluginList as slist
participant Plugin as plugin
participant PluginFactory as pfact
participant IQuimpPlugin as iPlugin

note over User : Cooperate with\nGUI by\nactionPerformed() and\n itemStateChanged()
note over QuimP : Main class instance
note over ser : Responsible for\nsaving and loading\nsupported classes
note over plugin : Internal representation\nof plugin instance
note over iPlugin : external instance\nof plugin

== Initialization ==
QuimP->pfact : <<create>>
note left
For details see 
**SnakePlugin**
document
end note
activate pfact
pfact->pfact : Scan for plugins

QuimP->slist : <<create>>(""NUM_SPLINE_PLUGINS""\n""pluginFactory""\n""viewUpdater"")
activate slist
slist->slist : setInstance
activate slist
slist->plugin : <<create>> at slot //i//
activate plugin
plugin -> iPlugin : <<create>>
activate iPlugin

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
ser->slist : beforeSerialize()
loop over plugins in SnakePluginList
    slist->plugin : downloadPluginConfig()
    plugin->iPlugin : ref.getVersion()
    iPlugin-->plugin : //version//
    plugin->iPlugin : ref.getPluginConfig()
    iPlugin-->plugin : //config//
end
slist-->ser
ser->ser : write

deactivate ser
destroy ser
deactivate plugin

== Load config ==
QuimP->ser : <<create>>
activate ser
QuimP->slist : closeAllWindows
loop over plugins in SnakePluginList
    slist->slist : getInstance(i)
    activate slist
    slist->iPlugin : ref.showUI(false)
    deactivate slist
end
ser->ser : load()
activate ser
ser->slist : afterSerialize()
loop over plugins in SnakePluginList
    slist->plugin : get old config
    note right
    Here is instance
    loaded and created
    by GSon
    endnote
    slist->slist : setInstance(**name**,..)
    activate slist
    slist->plugin : <<create>> at slot //i//
    activate plugin
    plugin->pfact : getInstance(**name**)
    destroy iPlugin
    pfact->iPlugin : <<create>>
    activate iPlugin
    pfact-->plugin : ref
    slist->iPlugin : ref.attachContext
    slist->iPlugin : ref.attachData
    deactivate slist
    slist->plugin : uploadPluginConfig()
    plugin->iPlugin : ref.setPluginConfig()
end
slist-->ser
ser-->QuimP : <<new instance>>
destroy ser
@enduml

## Remarks about loading QCONF {#rqconf}

*QCONF* file contains names of plugins that were used. Those plugins may not be present in current user configuration and this 
fact is noted by changing background of plugin selector to red. Here is how it is handled: Ususlly `SnakePluginList` holds names of all discovered plugins. Those names are populated to Choices during their initialization in \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildSetupPanel() "buildSetupPanel" by method \ref uk.ac.warwick.wsbc.QuimP.SnakePluginList.getPluginNames(int) "getPluginNames". Therefore in this workflow they both contain the same data (names). When external *QCONF* is loaded it can contain different names, not known for `SnakePluginList` nor `PluginFactory`. This situation is solved in \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.updateChoices() "updateChoices()" that is called on every screen refresh. In this method current name from `SnakePluginList` (loaded) is tried to be selected in `Choice`. If it was not known during BOA initialization it is not present in `Choice`. Therefore it can not be selected and this situation is detected by checking `Choice` state after try of selection. If it is `NONE` it means that given plugin name is not present there and thus it has not been found during BOA initialization. In this case this name is added to the list. When user tries to select this name he gets message from `SnakePluginList` and `PluginFactory` that this name is illegal and after next refreshing default `NONE` is selected in `Choice`.