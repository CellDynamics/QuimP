macro "CirCellDrawing"{

newImage("result", "8-bit Black", 512, 512, 21);
setForegroundColor(200, 200, 200);
var pos = 1;
for(i = 0; i <nSlices(); i++){
	pos  = pos + 20;
	makeOval(226, pos, 80, 80);
	run("Fill", "slice");
	run("Next Slice [>]");	
}

}