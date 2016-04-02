# ConfigurationHandling {#ConfigurationHandling}

\author p.baniukiewicz
\date 1 Apr 2016
\tableofcontents

# Description of use {#douconfig}

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