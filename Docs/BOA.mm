<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1439390467779" ID="ID_1133629997" MODIFIED="1439393002980" TEXT="BOA Plugin">
<node CREATED="1439390538551" ID="ID_1333994693" MODIFIED="1439390571713" POSITION="right" TEXT="BOA:Run">
<node CREATED="1439390641069" ID="ID_1832319511" MODIFIED="1439393038517" TEXT="Setup()">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">initializes main structures</font>
    </p>
  </body>
</html>
</richcontent>
<node CREATED="1439390942116" ID="ID_500299297" MODIFIED="1439393050740" TEXT="BOAp.setup()">
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
<node CREATED="1439391018372" ID="ID_1958582301" MODIFIED="1439391025308" TEXT="setups file name"/>
<node CREATED="1439391025681" ID="ID_1370340352" MODIFIED="1439391063361" TEXT="setups initial window parameter (internal)">
<node COLOR="#cc3300" CREATED="1439391068052" ID="ID_1058550189" MODIFIED="1439391942845" TEXT="why not use setDefaults method">
<icon BUILTIN="help"/>
</node>
</node>
</node>
<node CREATED="1439390778149" ID="ID_341733905" MODIFIED="1439392443884" TEXT="new Nest">
<icon BUILTIN="wizard"/>
<node CREATED="1439392423556" ID="ID_3376037" MODIFIED="1439392978877" TEXT="ArrayList&lt;SnakeHandler&gt;">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">array for all snakes on image</font>
    </p>
  </body>
</html>
</richcontent>
<font NAME="SansSerif" SIZE="12"/>
<icon BUILTIN="wizard"/>
<node CREATED="1439392464844" ID="ID_914431763" MODIFIED="1439392498044" TEXT="SnakeHandler stores table of snakes for one object for all frames"/>
</node>
</node>
<node CREATED="1439390791360" ID="ID_25836955" MODIFIED="1439392445764" TEXT="new ImageGroup">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390806152" ID="ID_792187126" MODIFIED="1439392447268" TEXT="new Canvas">
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
</html>
</richcontent>
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390857760" ID="ID_996000154" MODIFIED="1439392448884" TEXT="new RoiManager">
<icon BUILTIN="wizard"/>
</node>
<node CREATED="1439390864216" ID="ID_1212505514" MODIFIED="1439392450372" TEXT="new Constrictor">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">object for main snake evaluation</font>
    </p>
  </body>
</html>
</richcontent>
<icon BUILTIN="wizard"/>
</node>
</node>
<node CREATED="1439390696916" ID="ID_606943001" MODIFIED="1439393043764" TEXT="runBoa()">
<node COLOR="#cc3300" CREATED="1439391221620" ID="ID_859804983" MODIFIED="1439391937852" TEXT="nest.resetForFrame()">
<icon BUILTIN="help"/>
<node COLOR="#cc3300" CREATED="1439391972916" ID="ID_1952472435" MODIFIED="1439392909907" TEXT="what is the source of snakes at nest">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">they must be initialized from roi </font>
    </p>
  </body>
</html>
</richcontent>
<icon BUILTIN="help"/>
</node>
</node>
<node CREATED="1439391241080" ID="ID_915927212" MODIFIED="1439391276724" TEXT="constrictor.loosen() or implode()"/>
<node CREATED="1439391310440" ID="ID_1232397521" MODIFIED="1439391336028" TEXT="for all frames repeat">
<node CREATED="1439391349088" ID="ID_1091339617" MODIFIED="1439391358748" TEXT="nest.resetForFrame()"/>
<node CREATED="1439391364880" ID="ID_95228470" MODIFIED="1439391366476" TEXT="constrictor.loosen() or implode()"/>
<node CREATED="1439391411456" ID="ID_16883257" MODIFIED="1439392844621" TEXT="For each snake in Nest">
<node CREATED="1439391592532" ID="ID_441081194" MODIFIED="1439391724156" TEXT="get SnakeHandler for current snake from Nest">
<node COLOR="#338800" CREATED="1439391632733" ID="ID_1458929604" MODIFIED="1439392560619" TEXT="What is the difference between Snake and SnakeHAndler">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      <font size="2">SnakeHandler stores snake for every frame </font>
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node CREATED="1439391423677" ID="ID_1322423998" MODIFIED="1439391446052" TEXT="get this snake"/>
<node CREATED="1439391446440" ID="ID_1873435333" MODIFIED="1439391448209" TEXT="draw"/>
<node CREATED="1439392850284" ID="ID_1102844485" MODIFIED="1439393120700" TEXT="tightenSnake()"/>
<node CREATED="1439391448776" ID="ID_699364124" MODIFIED="1439393158459" TEXT=" storeCurrentSnake()">
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
</html>
</richcontent>
<icon BUILTIN="idea"/>
</node>
</node>
</node>
</node>
</node>
</node>
</map>
