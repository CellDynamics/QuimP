
# SnakePlugin {#SnakePlugin}

\author p.baniukiewicz
\date 19 Feb 2016
\tableofcontents

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

# Workflow diagrams {#workflowdiagrams}

The use case of snake plugin is as follows:

@startuml
left to right direction

:User: as user
rectangle "Plugin Service" {
    :QuimP: as quimp
    quimp--(Create Engine)
    (Create Engine) .> (Scan dir) : include
    user--(Run plugin)
    user--(Select plugin)
    user--(Show GUI)
    quimp--(Get plugin\nconfig)
    quimp--(Set plugin\nconfig)
}
note bottom of user : Related to GUI actions
note top of quimp : Plugin engine
note right of (Create Engine) : Initialization state
note "On load/write QuimP configuration" as N2
(Get plugin\nconfig) .. N2
N2 .. (Set plugin\nconfig)
@enduml

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

Activity diagram for use case **Select Plugin**. User selected one plugin on certain slot.
Plugins have been registered already by **Create Engine** use case.

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
