<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1439456586885" ID="ID_188552676" MODIFIED="1439456800054" TEXT="QuimP">
<node BACKGROUND_COLOR="#ff6666" CREATED="1439390467779" ID="ID_1133629997" MODIFIED="1439460230804" POSITION="right" STYLE="fork" TEXT="BOA Plugin">
<node CREATED="1439390538551" ID="ID_1333994693" MODIFIED="1439460230804" TEXT="BOA:Run()">
<node CREATED="1439390641069" ID="ID_1832319511" MODIFIED="1440497082320" TEXT="Setup()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">initializes main structures</font>
    </p>
  </body>
</html></richcontent>
<node CREATED="1439390942116" ID="ID_500299297" MODIFIED="1440592219197" TEXT="BOAp.setup()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">setup for default params etc</font>
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1439391018372" ID="ID_1958582301" MODIFIED="1439460230804" TEXT="setups file name"/>
<node CREATED="1439391025681" ID="ID_1370340352" MODIFIED="1439460230804" TEXT="setups initial window parameter (internal)">
<node COLOR="#cc3300" CREATED="1439391068052" ID="ID_1058550189" MODIFIED="1439460230804" TEXT="why not use setDefaults method">
<icon BUILTIN="help"/>
</node>
</node>
</node>
<node CREATED="1439390778149" ID="ID_341733905" MODIFIED="1439460230805" TEXT="new Nest">
<icon BUILTIN="wizard"/>
<node CREATED="1439392423556" ID="ID_3376037" MODIFIED="1439460230805" TEXT="ArrayList&lt;SnakeHandler&gt;">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">array for all snakes on image</font>
    </p>
  </body>
</html></richcontent>
<font NAME="SansSerif" SIZE="12"/>
<icon BUILTIN="wizard"/>
<node CREATED="1439392464844" ID="ID_914431763" MODIFIED="1439460230805" TEXT="SnakeHandler stores table of snakes for one object for all frames"/>
</node>
</node>
<node CREATED="1439390791360" ID="ID_25836955" MODIFIED="1439460230805" TEXT="new ImageGroup">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1440497089516" ID="ID_1165710913" MODIFIED="1440497113625" TEXT="new CustomStackWindow">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390806152" ID="ID_792187126" MODIFIED="1440497064173" TEXT="new CustomCanvas">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      
    </p>
    <p>
      <font size="2">creates main window </font>
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390857760" ID="ID_996000154" MODIFIED="1439460230805" TEXT="new RoiManager">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390864216" ID="ID_1212505514" MODIFIED="1440497099627" TEXT="new Constrictor">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">object for main snake evaluation</font>
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="wizard"/>
</node>
</node>
<node CREATED="1439390696916" ID="ID_606943001" MODIFIED="1440592312272" TEXT="runBoa()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      can be run from actionPerformed() as well. This method is called on reaction on interface
    </p>
  </body>
</html>
</richcontent>
<node COLOR="#cc3300" CREATED="1439391221620" ID="ID_859804983" MODIFIED="1439460230805" TEXT="nest.resetForFrame()">
<icon BUILTIN="help"/>
<node COLOR="#cc3300" CREATED="1439391972916" ID="ID_1952472435" MODIFIED="1439460230805" TEXT="what is the source of snakes at nest">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">they must be initialized from roi </font>
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="help"/>
</node>
</node>
<node CREATED="1439391241080" ID="ID_915927212" MODIFIED="1439460230805" TEXT="constrictor.loosen() or implode()"/>
<node CREATED="1439391310440" ID="ID_1232397521" MODIFIED="1439460230805" TEXT="for all frames repeat">
<node CREATED="1439391349088" ID="ID_1091339617" MODIFIED="1439460230805" TEXT="nest.resetForFrame()"/>
<node CREATED="1439391364880" ID="ID_95228470" MODIFIED="1439460230805" TEXT="constrictor.loosen() or implode()"/>
<node CREATED="1439391411456" ID="ID_16883257" MODIFIED="1439460230805" TEXT="For each snake in Nest">
<node CREATED="1439391592532" ID="ID_441081194" MODIFIED="1439460230806" TEXT="get SnakeHandler for current snake from Nest">
<node COLOR="#338800" CREATED="1439391632733" ID="ID_1458929604" MODIFIED="1439460230806" TEXT="What is the difference between Snake and SnakeHAndler">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">SnakeHandler stores snake for every frame </font>
    </p>
  </body>
</html></richcontent>
</node>
</node>
<node CREATED="1439391423677" ID="ID_1322423998" MODIFIED="1439460230806" TEXT="get this snake"/>
<node CREATED="1439391446440" ID="ID_1873435333" MODIFIED="1439460230806" TEXT="draw"/>
<node CREATED="1439392850284" ID="ID_1102844485" MODIFIED="1439460230806" TEXT="tightenSnake()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      match to shape - here is algorithm
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1439391448776" ID="ID_699364124" MODIFIED="1439460230806" TEXT=" storeCurrentSnake()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">every frame is stored for every snake (BOA_ 794) </font>
    </p>
    <p>
      <font size="2">store in Nest for current frame </font>
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="idea"/>
</node>
</node>
</node>
</node>
</node>
</node>
<node BACKGROUND_COLOR="#ff6666" CREATED="1439456841770" ID="ID_967529055" MODIFIED="1439460251348" POSITION="left" TEXT="ANA Plugin">
<node CREATED="1439456877162" ID="ID_1925471691" MODIFIED="1439460251348" TEXT="ANA:Run()">
<node CREATED="1439457245930" ID="ID_936531699" MODIFIED="1439460251348" TEXT="new Qparams">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439457267306" ID="ID_974364234" MODIFIED="1439460251348" TEXT="new OutlineHandler">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439457385850" ID="ID_597914816" MODIFIED="1439460251349" TEXT="ANAp.setup()">
<node CREATED="1439457483042" ID="ID_1162277685" MODIFIED="1439460251349" TEXT="Initializes parameters for algorithm and file names"/>
</node>
<node CREATED="1439457597187" ID="ID_361846116" MODIFIED="1439460251349" TEXT="FluoStats.read()">
<node CREATED="1439457606314" ID="ID_15108284" MODIFIED="1439460251349" TEXT="Read fluoroscence data from STATSFILE file "/>
</node>
<node CREATED="1439457698682" ID="ID_1589014015" MODIFIED="1439460251349" TEXT="investigateChannels(OutlineHandler::indexGetOutline"/>
<node CREATED="1439458090010" ID="ID_1555922133" MODIFIED="1439460251350" TEXT="outputH = new OutlineHandler"/>
<node CREATED="1439458034762" ID="ID_1202638648" MODIFIED="1439461387200" TEXT="Ana()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Ana uses several variables from class propagated among various methods and uses ECMM class as well
    </p>
  </body>
</html></richcontent>
<node CREATED="1439458100946" ID="ID_1581865465" MODIFIED="1439461446971" TEXT="OutlineHandler::save()"/>
</node>
<node CREATED="1439458126642" ID="ID_520322263" MODIFIED="1439460251350" TEXT="outputH.writeOutlines"/>
</node>
</node>
<node BACKGROUND_COLOR="#ff6666" CREATED="1439460178571" ID="ID_689289019" MODIFIED="1439460603844" POSITION="right" TEXT="ECMM Plugin">
<node CREATED="1439460587491" ID="ID_649396419" MODIFIED="1439460603845" TEXT="ECMM_Mapping()">
<node CREATED="1439460630987" ID="ID_181729402" MODIFIED="1439460637395" TEXT="get paQP file"/>
<node CREATED="1439460641027" ID="ID_67216783" MODIFIED="1439460662963" TEXT="QParams::readParams"/>
<node CREATED="1439460684747" ID="ID_1306943540" MODIFIED="1439460757074" TEXT="runFromFile()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Main executive for ECMM
    </p>
  </body>
</html></richcontent>
<node CREATED="1439460807899" ID="ID_461168764" MODIFIED="1439460811203" TEXT="new OutlineHandler()"/>
<node CREATED="1439460822835" ID="ID_1836746052" MODIFIED="1439460826314" TEXT="ECMp.setup()">
<node CREATED="1439460955875" ID="ID_1870194822" MODIFIED="1439460969213" TEXT="gets file names from previous analysis"/>
</node>
<node CREATED="1439460991228" ID="ID_908395997" MODIFIED="1439460994299" TEXT="ECMp.setParams()">
<node CREATED="1439461058003" ID="ID_882907622" MODIFIED="1439461064003" TEXT="sets default parameters"/>
</node>
<node CREATED="1439461086611" ID="ID_1204922426" MODIFIED="1439461092139" TEXT="new ECMplot">
<icon BUILTIN="wizard"/>
<node CREATED="1439461099035" ID="ID_598246772" MODIFIED="1439461106979" TEXT="initializes plotting"/>
</node>
<node CREATED="1439461204603" ID="ID_1048520019" MODIFIED="1439461216561" TEXT="run()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      Main executive for ECMM
    </p>
  </body>
</html></richcontent>
<node CREATED="1439461417051" ID="ID_1588331953" MODIFIED="1439461430675" TEXT="OutlineHandler::save()"/>
</node>
<node CREATED="1439461238667" ID="ID_761483804" MODIFIED="1439461249971" TEXT="OutlineHandler::writeOutlines()"/>
</node>
<node CREATED="1439460725107" ID="ID_1591513174" MODIFIED="1439460738251" TEXT="Process other particular cell files">
<node CREATED="1439460747115" ID="ID_78991993" MODIFIED="1439460748442" TEXT="runFromFile()"/>
</node>
</node>
</node>
<node BACKGROUND_COLOR="#009933" CREATED="1439465008500" ID="ID_1334008451" MODIFIED="1439465138221" POSITION="left" STYLE="bubble" TEXT="Questions">
<icon BUILTIN="help"/>
<node CREATED="1439465036124" ID="ID_1872766014" MODIFIED="1439465121420" TEXT="How snake is initialized from ROI?">
<node CREATED="1439465097780" ID="ID_1960673714" MODIFIED="1439465110052" TEXT="How node list build?"/>
</node>
</node>
</node>
</map>
