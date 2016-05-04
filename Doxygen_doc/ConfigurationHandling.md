# ConfigurationHandling {#ConfigurationHandling}

\author p.baniukiewicz
\date 1 Apr 2016
\tableofcontents

# General file operations {#genfile}
File names are defined in configuration class \ref uk.ac.warwick.wsbc.QuimP.BOAp "BOAp" (see comments in Doxygen doc). The very general workflow for saving and loading configurations for particular parts of QuimP is presented below.

# BOA {#fileboa}
BOA produces several files:
1. *paQP* - main configuration file, contains segmentation parameters 
2. *snQP* - contains Snake objects for every frame, separate file for every Snake
3. *stQP* - statistic file
4. **pgQP** - new configuration file, holds plugin parameters (see @ref genfile)
5. **newsnQP** - new configuration file, contains all Snakes for all frames (dump of \ref uk.ac.warwick.wsbc.QuimP.BOA_.BOAState "BOAState") and all configuration related to segmentation

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
note left #aqua : Write //paQP// file
BOAp -> QParams : <<create>>
activate QParams
BOAp-->QParams : //set all fields//
QParams -> QParams : writeParams()
QParams --> BOAp
destroy QParams
end
"BOA::Finish()" -> Nest: analyse()
note left #aqua : Write //stQP// file
"BOA::Finish()" -> Serializer : <SnakePluginList>
Serializer -> Serializer : save()
note left #aqua : Write //pgQP// file
"BOA::Finish()" -> Serializer : <BOAState>
Serializer -> Serializer : save()
note left #orange : Write //newsnQP// file
@enduml

# Description of use for SnakePluginList {#douconfig}

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