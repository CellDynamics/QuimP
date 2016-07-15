# Binary segmentation {#BinarySeg}

\author p.baniukiewicz
\date 28 Jun 2016
\tableofcontents

# About

QuimP supports converting black-white masks into Snakes making possible to use variety of external
segmentation algorithms.

# Segmentation algorithm details {#segalgdet}

For details about the algorithm see:

1. \ref uk.ac.warwick.wsbc.QuimP.geom.TrackOutline "TrackOutline" - converting objects to ROIs in one frame
2. \ref uk.ac.warwick.wsbc.QuimP.BinarySegmentation "BinarySegmentation" - grouping ROIs in chains related to \ref uk.ac.warwick.wsbc.QuimP.SnakeHandler "SnakeHandler"
3. \ref uk.ac.warwick.wsbc.QuimP.geom.SegmentedShapeRoi "SegmentedShapeRoi" - holds extra information about ROi and converts it to list of points

# Implementation in QuimP-BOA

The algorithm mentioned in \ref segalgdet "previous chapter" is packed into \ref uk.ac.warwick.wsbc.QuimP.BinarySegmentationPlugin "BinarySegmentationPlugin" class that implements \ref uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin "IQuimpCorePlugin" interface. This interface allows to
modify Nest objects directly.

The \ref uk.ac.warwick.wsbc.QuimP.BinarySegmentationPlugin "BinarySegmentationPlugin" is not maintained by 
any container-like class as it is for snake plugins (e.g. \ref uk.ac.warwick.wsbc.QuimP.SnakePluginList).
The instance of \ref uk.ac.warwick.wsbc.QuimP.BinarySegmentationPlugin "BinarySegmentationPlugin" is created in
\ref uk.ac.warwick.wsbc.QuimP.BOAState "BOAState" as *transient* variable, **not initialized until first use**. 
There is also second player, the variable `binarySegmentationParam` that holds parameters of Binary segmentation.
This field is initialized in `BOAState` constructor by empty list and in \ref uk.ac.warwick.wsbc.QuimP.BOAState.beforeSerialize() "beforeSerialize" method where current parameters are gathered from plugin.

## Run segmentation

Segmentation is run by user by clicking menu entry. The code is under \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(ActionEvent)

@startuml
start
:create object;
:attach ""Nest"";
:attach ""ViewUpdater"";
:show UI;
end
@enduml

Note, that there is no direct call of `runPlugin` method. This is because this method is called by plugin internally on every use of *Apply* button.

@startuml
actor User
User-->BOA : start segmentation
BOA->BinarySegmentationPlugin : <<create>>\nattach data
activate BinarySegmentationPlugin
BinarySegmentationPlugin->BinarySegmentationPlugin : buildUI
BinarySegmentationPlugin-->BOA
BOA->BinarySegmentationPlugin : showUI
== ==
User-->BinarySegmentationPlugin : click //Apply//
BinarySegmentationPlugin->runPlugin
runPlugin->runPlugin : build new ""Nest""
runPlugin-->BOA : updateView
runPlugin-->BinarySegmentationPlugin
BinarySegmentationPlugin->BinarySegmentationPlugin : build ""ParamList""
== ==
User-->BOA : save and quit
BOA->BinarySegmentationPlugin : getPluginConfig
BinarySegmentationPlugin-->BOA : ""ParamList""
@enduml    

The #ref uk.ac.warwick.wsbc.QuimP.plugin.ParamList "ParamList" gathers all standard plugin options and some additional. It is used for exchanging data between BOA_ and plugin, mainly for archiving purposes (see \ref confhand "this chapter").

## Configuration handling {#confhand}

This plugin supports one directional configuration handling. It can be saved in *QCONF* file but it is not restored on load. As a part of serializable \ref uk.ac.warwick.wsbc.QuimP.BOAState "BOAState" class, the configuration is stored through `binarySegmentationParam` which is filled in \ref uk.ac.warwick.wsbc.QuimP.BOAState.beforeSerialize() "beforeSerialize". The following states of this field are possible:

1. Can be empty `ParamList` - when user does not run binary Segmentation at all
2. Can contain some data - when user run (opened window at least) of the plugin but does not click *Apply* or there has not been valid data (e.g. loaded mask file)
3. Can contain some data **and** name of the mask file - user used the plugin

To reach this, the ParamList with configuration inside the plugin is build in a little different way.
The file name is not the part of `QWindowBuilder` thus it is added separately in derived `BinarySegmentationPlugin`. The list is formed in \ref uk.ac.warwick.wsbc.QuimP.BinarySegmentationPlugin.actionPerformed(ActionEvent) "Apply" button to make sure that it contains last used data (without this `maskFilename` could be e.g. loaded file when user then clicked *Cancel* and plugin has not run at all).