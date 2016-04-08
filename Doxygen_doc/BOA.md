
# BOA

\author p.baniukiewicz
\date 30 Nov 2015
\tableofcontents
\todo Review code about creating snakes after recent changes in SnakeHandler


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

This section describes basic activities that occur during starting the BOA plugin. The entry function of BOA is overrode from ij.plugin.PlugIn the \ref uk.ac.warwick.wsbc.QuimP.BOA_.run(final String) "run()" method.

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

The \ref uk.ac.warwick.wsbc.QuimP.BOA_.run(final String) "run()" method checks general prerequirements for plugin such as IJ version, input image type. Then the static field \c BOA_.running is set to \c true to prevent from running more instances of plugin. Then the following methods are started to configure GUI and start segmentation.

@msc
	hscale="0.5";
    BOA_;
    BOA_=>BOA_ [label="setup(ImagePlus)"];
    BOA_=>BOA_ [label="about()"];
    BOA_=>BOA_ [label="runBoa(int,int)"];
@endmsc

In general the \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(final ImagePlus) "setup(ImagePlus)" method build user interface and sets default parameters for segmentation (see \ref setupBOA, \ref guiBOA, \ref setupBOAGUI  ), \ref uk.ac.warwick.wsbc.QuimP.BOA_.about() "about()" just shows info in log window and \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBOA(int,int)" is responsible for segmentation. 
`runBOA` is called on startup to run segmentation in case of filled ROI manager.  

## Setup BOA plugin {#setupBOA}

### General workflow {#setupBOAGeneral}

The initial setup of BOA environment is mostly done in \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(final ImagePlus) "setup(ImagePlus)" method (see call graph). The following operations occur during this stage:

@dot
digraph SetupRelations {
rankdir = LR;
node [shape=box,fontsize=11];
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

First, the method initializes internal and external default parameters hold at uk.ac.warwick.wsbc.QuimP.BOAp (see \ref setupBOABOAp) \b static class by calling \ref uk.ac.warwick.wsbc.QuimP.BOAp.SegParam.setDefaults() "setDefaults()" and \ref uk.ac.warwick.wsbc.QuimP.BOAp.setup(final ImagePlus) "setup(ImagePlus)" from BOAp class. By internal parameters I understand those that can not be changed by user during using the program. External parameters are those that are reflected in user menu.

Secondly, it creates all important structures such as:

- uk.ac.warwick.wsbc.QuimP.Nest.Nest()
- uk.ac.warwick.wsbc.QuimP.ImageGroup.ImageGroup(ImagePlus, Nest)
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas.CustomCanvas(ImagePlus)
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.CustomStackWindow(ImagePlus, ImageCanvas)

Then the main window is constructed (referenced by uk.ac.warwick.wsbc.QuimP.BOA_.window field) and listener for closing action is added.

Next the image is calibrated using real units by calling \ref uk.ac.warwick.wsbc.QuimP.BOA_.setScales() "BOA_.setScales()", \ref uk.ac.warwick.wsbc.QuimP.BOA_.updateImageScale() "BOA_.updateImageScale()" and \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.setScalesText() "BOA_.CustomStackWindow.setScalesText()".

Finally \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(final ImagePlus) "setup(ImagePlus)" method gets ROI from ROI manager and passes it to \ref uk.ac.warwick.wsbc.QuimP.Nest "Nest" class by \ref uk.ac.warwick.wsbc.QuimP.Nest.addHandler(final Roi, int) "Nest.addHandler(Roi, int)" method. This ROI should contain object for segmentation. This step is not obligatory and depends on content of ROI manager at BOA plugin start.

#### BOA state machine {#setupBOABOAp}

Most of configuration parameters are initialized ad stored in static class uk.ac.warwick.wsbc.QuimP.BOAp. They are initialized in \ref uk.ac.warwick.wsbc.QuimP.BOA_.setup(final ImagePlus) "setup(ImagePlus)" stage as mentioned in \ref setupBOA. Currently uk.ac.warwick.wsbc.QuimP.BOAp.setup(ImagePlus) method initializes BOAp fields with current image data and some constants related to active contour algorithm and boolean semaphores. Very similar method uk.ac.warwick.wsbc.QuimP.BOAp.setDefaults() initializes default values of active contour algorithm exposed to user (it is called from uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.setDefualts() as well).

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

The main method that runs segmentation is uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) called by uk.ac.warwick.wsbc.QuimP.BOA_.run(final String) on plugin start if there is snake in Nest or by clicking GUI elements (\ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent) "actionPerformed", \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.itemStateChanged(final ItemEvent) "itemStateChanged", \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.stateChanged(final ChangeEvent) "stateChanged").

### BOA structures {#runBOAStructures}

The following structures are used to hold and process segmented data:

1. \ref uk.ac.warwick.wsbc.QuimP.Nest "Nest" - this class holds segmented objects. Every selected cell on image is kept inside this class at `sHs` list.
2. \ref uk.ac.warwick.wsbc.QuimP.SnakeHandler "SnakeHandler" - this class stores snakes in `snakes` array. It operates with two forms of snakes. The first one is `Snake liveSnake` which is snake object being processed, the second one is array `Snake[] snakes` where already processed snakes are stored. This array has size of `FRAMES-f`, where `f` is the frame which object has been added for segmentation in. `SnakeHandler` holds all snake objects for one cell for all successive frames after frame where cell has been added. This class also performs writing/reading operations. Once initialized this object contains only `live` snakes which are modified by segmentation methods from `BOA`. After modification they are **copied** to `snakes[]` array. The `snakes[]` array is the array displayed on screen. There are two ways fo snakes to get there. First one is by method `storeLiveSnake` and second one is by method `storeThisSnake`. The difference is that the first one just copies internal `liveSnake` to array `snakes[]` whereas the second one copies external `Snake` to this array. `storeThisSnake` is used in plugin processing.
3. \ref uk.ac.warwick.wsbc.QuimP.Snake "Snake" this class holds snake for one frame and it is responsible for preparing segmentation process basing on passed ROI. **It does not do segmentation itself** but provides functions for scaling, changing orientation, cutting loops, etc.
4. \ref uk.ac.warwick.wsbc.QuimP.Node "Node" represents vertex on snake and allows to add extra properties (forces, normals) to them. It has form of linked list, every Node knows its predecessor and successor. Simplified diagram below shows basic class relations and most important methods and fields. This class has `prelimPoint` field that keeps preliminary vector values which can be later promoted to regular vale of current object by using \ref uk.ac.warwick.wsbc.QuimP.Node.update() "update()" method. **List is looped - last element points to first and first to last** 

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
	SnakeHandler : +void storeLiveSnake(frame)
	Snake : -Node head
	Snake : +void Snake(roi, id, direction)
	Snake : -void addNode(Node)
	Node : -Node prev	
    Node : -Node next
	Node : +Node(nr)
@enduml

Every Snake has its head which is considered as first Node in list. Many Snake methods use this 
fact to iterate over the whole list. Information about head is stored in **TWO** locations. First, the
head Node has set flag **head**, second this Node is assigned to *head* in Snake object. Synchronization
must be maintained by programmer.  

### Creating Snakes {#createsnakes}

Snakes objects are created for every frame separately and collected in object `SnakeHandler`. Every segmented cell is stored in top class `Nest` at `sHs` array. The array `snakes` at `SnakeHandler` is size of remaining frames from current one (that which the cell was added at) to the end of stack.

@dot
digraph CreateSnake {
    rankdir = LR;
    node [shape=box,fontsize=11];
     
    BOASetup [label="BOA::Setup"];
    BOAAddcell [label="BOA::addCell"];
    nestaddHandler [label="nest::addHandler(roi,frame)"];
    GUI [style=filled, fillcolor=yellow];
    onRUN [style=filled, fillcolor=yellow];
    sh [label="new SnakeHandler(roi, frame, ID);"];
    s [label="new Snake[frames_to_end];\nliveSnake = new Snake(roi, ID, direction)"];
    n [label="create linked list\nof Node"];
 
    GUI->BOAAddcell;
    onRUN->BOASetup;
    BOASetup->nestaddHandler;
    BOAAddcell->nestaddHandler;
    nestaddHandler->sh;
    sh->s->n;
}
@enddot

The \ref uk.ac.warwick.wsbc.QuimP.Snake "Snake" object store only head of snake as \ref uk.ac.warwick.wsbc.QuimP.Node "Node" object that is two directional linked list. This list is initialized in appropriate methods of `Snake` that convert passed *ROI* to form of `Node` list (see e.g. \ref uk.ac.warwick.wsbc.QuimP.Snake.intializePolygon(final FloatPolygon) "intializePolygon"). Snake is created in \ref uk.ac.warwick.wsbc.QuimP.Snake "Snake" class constructor and there are three possibilities here:

@dot
digraph CreateSnake1 {
	rankdir = LR;
    node [shape=box,fontsize=11];
    
    Snake [label="Snake constructors",style=filled,fillcolor=chocolate];
    addNode [label="addNode(Node)",style=filled,fillcolor=darkgreen];
    
    Snake->intializeFloat [label=" Not used?",fontsize=11];
    Snake->intializeOval [label=" If input ROI is other than polygon",fontsize=11];
    Snake->intializePolygon [label=" If input ROI is polygon\nand nodes are refined",fontsize=11];
    Snake->intializePolygonDirect [label=" If input ROI is polygon\nand nodes are not refined",fontsize=11];
    intializeFloat->addNode;
    intializeOval->addNode;
    intializePolygon->addNode;
    intializePolygonDirect->addNode;
    addNode->updateNormales;
}
@enddot

The \ref uk.ac.warwick.wsbc.QuimP.Snake.intializePolygon(final FloatPolygon) "intializePolygon" creates first estimation of snake using ROI shape and doing node refinement to get number of nodes for every edge as defined in uk.ac.warwick.wsbc.QuimP.BOAp.nodeRes. \ref uk.ac.warwick.wsbc.QuimP.Snake.intializePolygonDirect(final FloatPolygon) "intializePolygonDirect" does not refine points and uses those from polygon. If input ROI is other type than `RECTANGLE` or `POLYGON` as the firs estimation of segmented shape the ellipse is used calculated by \ref uk.ac.warwick.wsbc.QuimP.Snake.intializeOval(int, int, int, int, int, double) "intializeOval" according to initial parameters based on ROI bounding box.  

The \ref uk.ac.warwick.wsbc.QuimP.Snake.addNode(final Node) "addNode(Node)" method constructs Snake as it is called for every node calculated in above methods. It is important that \ref uk.ac.warwick.wsbc.QuimP.Snake.addNode(final Node) "addNode(Node)" is used only for Snake initialization and together with correct initial conditions:

@code{.java}
head = new Node(0); //make a dummy head node for list initialization
head.setPrev(head); // link head to itself
head.setNext(head);
head.setHead(true);
// run more than 3 times
	node = new Node(x,y,i++);
	addNode(node);
// ----
removeNode(head); // remove dummy head node
@endcode

it guarantees the created list is looped. Last Node points to first by \ref uk.ac.warwick.wsbc.QuimP.Node.getPrev() "Node::getPrev()" and first one points to last one by \ref uk.ac.warwick.wsbc.QuimP.Node.getNext() "Node::getNext()".   

### Initiating segmentation 

The segmentation is started after initialization described at \ref createsnakes, \ref runBOAStructures and \ref setupBOAGeneral. Process of segmentation of whole stack is started by clicking:

- \ref uk.ac.warwick.wsbc.QuimP.BOA_.addCell(final Roi, int) "addCell" - refers to \ref uk.ac.warwick.wsbc.QuimP.BOA_.tightenSnake(final Snake) "tightenSnake" that performs `Snake` modification and fits it to cell shape. `addCell` performs basic segmentation.`tightenSnake` is segmentation procedure that modifies `liveSnake` obtained from `SnakeHandler`. After modification this Snake is stored back to SnakeHandler to `snakes[f]` array deleting snake at index `f`. The method \ref uk.ac.warwick.wsbc.QuimP.SnakeHandler.storeLiveSnake(int) "storeLiveSnake(int)" copies whole linked list given by `head` to mentioned table closing the Snake.  
- **Segmentation** calls \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBOA" method.
- Modifying other parameters of GUI that result in calling \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBOA" method

Those methods are called from \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent) "actionPerformed" method.


### Segmentation

Segmentation is controlled by \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBOA" that is ran over certain frame range. The following operations have place for every frame from current to last one in stack:

@dot
digraph segment {
	rankdir = LR;
	node [shape=box,fontsize=11];
	
	nestresetForFrame [label="nest::resetForFrame"];
	constrictorloosen [label="constrictor::loosen(nest, startF)"];
	constrictorimplode [label="constrictor::implode(nest, startF)"];
	imageGroupdrawPath [label="imageGroup::drawPath(snake, frame)"]; 
	tightenSnake [label="tightenSnake(snake)",style=filled,fillcolor=yellow];
	sHstoreLiveSnake [label="SnakeHandler::storeLiveSnake(frame)"];
	
	nestresetForFrame->constrictorloosen->imageGroupdrawPath;
	nestresetForFrame->constrictorimplode->imageGroupdrawPath;
	imageGroupdrawPath->tightenSnake->sHstoreLiveSnake;
}
@enddot

#### Segmentation algorithm

@todo Finish here and say how snakes are created as well
   

## About GUI {#guiBOA}

Class uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas is responsible for handling mouse events whereas class uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow holds definition of user controls. Mouse events serviced by \ref uk.ac.warwick.wsbc.QuimP.BOA_.CustomCanvas "CustomCanvas" class are related to main paint window where cells are drawn. Three main methods that respond to action on GUI are:

- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.actionPerformed(final ActionEvent) - related to buttons and general GUI logic
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.itemStateChanged(final ItemEvent) - related to changes of checkboxes
- uk.ac.warwick.wsbc.QuimP.BOA_.CustomStackWindow.stateChanged(final ChangeEvent) - related to spinners

All these method can run segmentation of current frame or whole stack by calling \ref uk.ac.warwick.wsbc.QuimP.BOA_.runBoa(int, int) "runBoa(int, int)".
