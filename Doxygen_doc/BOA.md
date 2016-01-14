
# BOA

\author p.baniukiewicz
\date 30 Nov 2015
\tableofcontents

# BOA Plugin {#BOA}

## General information {#GeneralBOA}

BOA plugin performs image segmentation using Active Contour method. The main plugin class is uk.ac.warwick.wsbc.QuimP.BOA_ where one can find also full collaboration diagram. This class contains three nested classes:

- uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomWindowAdapter

that control main window creation process.

## General relationships {#importantStructuresBOA}

There are several classes and structures referenced in \ref uk.ac.warwick.wsbc.QuimP.BOA_ "BOA_" class. These can be split to groups:

- Related to segmentation
 - uk.ac.warwick.wsbc.QuimP.Constrictor
 - uk.ac.warwick.wsbc.QuimP.Nest
 - uk.ac.warwick.wsbc.QuimP.SnakeHandler
 - uk.ac.warwick.wsbc.QuimP.Snake
 - uk.ac.warwick.wsbc.QuimP.Node
- Related to GUI
 - uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow. It uses object \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomWindowAdapter "CustomWindowAdapter" (Collection relationship) created in uk.ac.warwick.wsbc.QuimP.BOA_.setup() method and passed to \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomWindowAdapter "CustomWindowAdapter" throught \c addWindowListener() method. 
 - uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas (composition of \ref uk.ac.warwick.wsbc.QuimP.BOA_ "BOA_")
- Related to images
 - uk.ac.warwick.wsbc.QuimP.ImageGroup (composition of \ref uk.ac.warwick.wsbc.QuimP.BOA_ "BOA_")

@todo add general description - what classes what do and in what order, eg - setup, then roi passed to nest and ..., add plot

## Starting BOA plugin {#startBOA}

This section describes basic activities that occur during starting the BOA plugin. The entry function of BOA is overrode from ij.plugin.PlugIn the \ref uk.ac.warwick.wsbc.QuimP.BOA_.run(String) "run()" method.

Some important structures are initialized in \a BOA_ class as its private or public members. Those are:

@code{.java}
   CustomCanvas canvas;
   CustomStackWindow window;
   static TextArea logArea;
   static boolean running = false;
   ImageGroup imageGroup;
   private Nest nest;
   private int frame;
   private Constrictor constrictor;
@endcode   

The \ref uk.ac.warwick.wsbc.QuimP.BOA_.run(String) "run()" method checks general prerequirements for plugin such as IJ version, input image type. Then the static field \c BOA_.running is set to \c true to prevent from running more instances of plugin. Then the following methods are started to configure GUI and start segmentation.

@msc
	hscale="0.5";
    BOA_;
    BOA_=>BOA_ [label="setup(ImagePlus)"];
    BOA_=>BOA_ [label="about()"];
    BOA_=>BOA_ [label="runBoa(int,int)"];
@endmsc

In general the \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(ImagePlus) "setup(ImagePlus)" method build user interface and sets default parameters for segmentation (see \ref setupBOA ), \ref uk.ac.warwick.wsbc.QuimP.BOA_.about() "about()" just shows info in log window and \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBOA(int,int)" is responsible for segmentation. 
`runBOA` is called on startup to run segmentation in case of filled ROI manager.  

## Setup BOA plugin {#setupBOA}

### General workflow {#setupBOAGeneral}

The initial setup of BOA environment is mostly done in \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(ImagePlus) "setup(ImagePlus)" method (see call graph). The following operations occur during this stage:

@dot
digraph SetupRelations {
rankdir = LR;
node [shape=box,fontsize=10];
Start[shape=ellipse,style=filled, fillcolor=gray];
Stop[shape=ellipse,style=filled, fillcolor=gray];
Init [label="Initialize\nstructures"];
InitParam [label="Initialize\ndefault parameters"];
IJconfig [label="Configuration\nof ImageJ"];
BuildWnd [label="Construct\nwindow and\nadd listener"];
AddSnake [label="Add initial\nROI to nest",style=filled, fillcolor=yellow];
Start->InitParam->Init->BuildWnd->IJconfig->CreateSnake->Stop;
}
@enddot

First, the method initializes internal and external default parameters hold at uk.ac.warwick.wsbc.QuimP.BOAp (see \ref setupBOABOAp) \b static class by calling \ref uk.ac.warwick.wsbc.QuimP.BOAp.setDefaults() "setDefaults()" and \ref uk.ac.warwick.wsbc.QuimP.BOAp.setup(ImagePlus) "setup(ImagePlus)" from BOAp class. By internal parameters I understand those that can not be changed by user during using the program. External parameters are those that are reflected in user menu.

Secondly, it creates all important structures such as:

- uk.ac.warwick.wsbc.QuimP.Nest.Nest()
- uk.ac.warwick.wsbc.QuimP.ImageGroup.ImageGroup(ImagePlus, Nest)
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas.CustomCanvas(ImagePlus)
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.CustomStackWindow(ImagePlus, ImageCanvas)

Then the main window is constructed (referenced by uk.ac.warwick.wsbc.QuimP.BOA_.window field) and listener for closing action is added.

Next the image is calibrated using real units by calling \ref uk.ac.warwick.wsbc.QuimP.BOA_.setScales() "BOA_.setScales()", \ref uk.ac.warwick.wsbc.QuimP.BOA_.updateImageScale() "BOA_.updateImageScale()" and \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.setScalesText() "BOA_.CustomStackWindow.setScalesText()".

Finally \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(ImagePlus) "setup(ImagePlus)" method gets ROI from ROI manager and passes it to \ref uk.ac.warwick.wsbc.QuimP.Nest "Nest" class by \ref uk.ac.warwick.wsbc.QuimP.Nest.addHandler(Roi, int) "Nest.addHandler(Roi, int)" method. This ROI should contain object for segmentation. This step is not obligatory and depends on content of ROI manager at BOA plugin start.

#### BOA state machine {#setupBOABOAp}

Most of configuration parameters are initialized ad stored in static class uk.ac.warwick.wsbc.QuimP.BOAp. They are initialized in \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(ImagePlus) "setup(ImagePlus)" stage as mentioned in \ref setupBOA. Currently uk.ac.warwick.wsbc.QuimP.BOAp.setup(ImagePlus) method initializes BOAp fields with current image data and some constants related to active contour algorithm and boolean semaphores. Very similar method uk.ac.warwick.wsbc.QuimP.BOAp.setDefaults() initializes default values of active contour algorithm exposed to user (it is called from uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.setDefualts() as well).

This class stands also as wrapper for reading and writing configuration from/to disk. Low level disk operations are supported by container class uk.ac.warwick.wsbc.QuimP.QParams

### Building GUI {#setupBOAGUI}

GUI is created in void uk.ac.warwick.wsbc.QuimP.BOA_#setup() method. One can find the following code responsible for building and displaying GUI window.

@code{.java}
      canvas = new CustomCanvas(imageGroup.getOrgIpl());
      window = new CustomStackWindow(imageGroup.getOrgIpl(), canvas);
      window.buildWindow();
@endcode

The main builder is method \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildWindow() "buildWindow()" which calls two other methods:

- \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildControlPanel() "buildControlPanel()"
- \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildSetupPanel() "buildSetupPanel()"

The \b ControlPanel is on the left of main window and contains all parameter fields, \a Load, \a Default, \a Edit, \a Segment, etc controls. The \b SetupPanel is on right of main window and contains Log field, Interval/Scale infos and all buttons.

Inside \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.buildSetupPanel() "buildSetupPanel()" one can find main panel (\c setupPanel) comprised from three sub-panels called \c northPanel, \c southPanel and \c logPanel which in centre of of \c setupPanel. These objects contain:

- \c northPanel: static fields, Scale, Truncate, Add, Delete buttons
- \c southPanel: Quit, Finish buttons
- \c logPanel: log Area

Any action on these controls is handled by uk.ac.warwick.wsbc.QuimP_11b.BOA_.CustomStackWindow.actionPerformed() method. The following buttons run actions:
- \ref editBOA

#### Edit {#editBOA}

When user click \b Edit \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed() "actionPerformed(ActionEvent)" method catch this and calls uk.ac.warwick.wsbc.QuimP.BOA_.editSeg(int, int, int) method if there is only one snake on screen. If there is more snakes, user must click one of them to perform its editing. Second click of \b Edit switches editing off by calling uk.ac.warwick.wsbc.QuimP.BOA_.stopEdit(). 

@todo Add remaining buttons here

## Running BOA segmentation {#runBOA}

The BOA segmentation can be run in following ways:

The main method that runs segmentation is uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) called by uk.ac.warwick.wsbc.QuimP.BOA_.run(String) on plugin start if there is snake in Nest or by clicking GUI elements (\ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(ActionEvent) "actionPerformed", \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.itemStateChanged(ItemEvent) "itemStateChanged", \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.stateChanged(ChangeEvent) "stateChanged").

### BOA structures {#runBOAStructures}

The following structures are used to hold and process segmented data:

1. \ref uk.ac.warwick.wsbc.QuimP.Nest "Nest" - this class holds segmented objects. Every selected cell on image is kept inside this class at `sHs` list.
2. \ref uk.ac.warwick.wsbc.QuimP.SnakeHandler "SnakeHandler" - this class stores snakes in `snakes` array. It operates with two forms of snakes. The first one is `Snake liveSnake` which is snake object being processed, the second one is array `Snake[] snakes` where already processed snakes are stored. This array has size of `FRAMES-f`, where `f` is the frame which object has been added for segmentation in. `SnakeHandler` holds all snake objects for one cell for all successive frames after frame where cell has been added. This class also performs writing/reading operations.
3. \ref uk.ac.warwick.wsbc.QuimP.Snake "Snake" this class holds snake for one frame and it is responsible for preparing segmentation process basing on passed ROI. **It does not do segmentation itself** but provides functions for scaling, changing orientation, cutting loops, etc.
4. \ref uk.ac.warwick.wsbc.QuimP.Node "Node" represents vertex on snake and allows to add extra properties (forces, normals) to them. It has form of linked list, every Node knows its predecessor and successor. Simplified diagram below shows basic class relations and most important methods and fields.

@startuml "Simplified class structure for Snakes" 
	Nest*-->SnakeHandler : sHs[cells]
	SnakeHandler*-->Snake : snakes[frames], liveSnake
	Snake*-->Node : head
	Node-->Node
	Nest : -ArrayList<SnakeHandler> sHs
	Nest : +addHandler(roi,frame)
	SnakeHandler : -Snake liveSnake;
    SnakeHandler : -Snake[] snakes;
	SnakeHandler : +void SnakeHandler(roi, frame, id)
	SnakeHandler : +void storeCurrentSnake(frame)
	Snake : -Node head
	Snake : +void Snake(roi, id, direction)
	Snake : -void addNode(Node)
	Node : -Node prev	
    Node : -Node next
	Node : +Node(nr)
@enduml

### Create Snakes {#createsnakes}

Snakes objects are created for every frame separately and collected in object `SnakeHandler`. Every segmented cell is stored in top class `Nest` at `sHs` array. The array `snakes` at `SnakeHandler` is size of remaining frames from current one (that which the cell was added at) to the end of stack.

### Initiating segmentation 
   

## About GUI {#guiBOA}

Class uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas is responsible for handling mouse events whereas class uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow holds definition of user controls. Mouse events serviced by \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas "CustomCanvas" class are related to main paint window where cells are drawn. Three main methods that respond to action on GUI are:

- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(ActionEvent) - related to buttons and general GUI logic
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.itemStateChanged(ItemEvent) - related to changes of checkboxes
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.stateChanged(ChangeEvent) - related to spinners

All these method can run segmentation of current frame or whole stack by calling \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBoa(int, int)".
