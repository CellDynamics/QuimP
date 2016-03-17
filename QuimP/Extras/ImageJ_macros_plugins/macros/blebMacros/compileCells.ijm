macro "compileCell" {
// open 2 channel original tiff. select around cell. add to manager
// and decide on frame range you want to use

cellRange = newArray( -1, -1,    -1,-1,      -1,-1,        -1,-1);
scaleAmount = 2;
strain = "Pikl-KO";
date = "091210";
movieIndex = "26";
experimentID = "7";
savePath = "/home/richardtyson/Documents/blebbing/PikI-ABD-50sec/"

print("\\Clear");

frameInterval = call("AddOns.getFrameInterval");
nbCells = roiManager("count");
if(nbCells == 0) {
	if(selectionType()==-1){
		exit("No selections");; 
	}
	roiManager("Add");	
}

orIm = getImageID();

orFile = getDirectory("image") + getInfo("image.filename"); //all files
//close();
negFile = orFile + ".neg.tif";
segFile = orFile + ".seg.tif";
actinFile = orFile + ".act.tif";
movieID = date + strain;

print("here");
//setBatchMode(true);

for( c = 1 ; c <= nbCells ; c++){
	
	//make save directory
	saveName = movieID + "_" + movieIndex + "_" + c;
	saveDir = savePath + "" + saveName;
	
	if(File.exists(saveDir)==1){
		//File.delete(saveDir);
		exit("Folder " + saveDir + " already exists");
	}
	File.makeDirectory(saveDir);

	print("Extracting cell " + c + " to: "+saveName);

	open(negFile);
	
	//set range to max if -1,-1
	a = cellRange[2*(c-1)];
	b = cellRange[(2*(c-1))+1];
	if( a == -1 && b == -1){
		a = 1;
		b = nSlices();
	}else if(a * b < 0){
		exit("Error in range values + " a + "' " + b);
	}

	// save crop location etc
	infoFile = File.open(saveDir +"/"+saveName + "_info.txt");
	print(infoFile,"#source file\n");
	print(infoFile,orFile+"\n");
	getSelectionBounds(x,y,w,h);
	print(infoFile, "#crop location (x,y,width,height)\n");
	print(infoFile, "#makeRectangle(" + x+","+y+","+w+","+h+')');
	print(infoFile, x+","+y+","+w+","+h+"\n");
	print(infoFile, "# Frame range utilized");
	print(infoFile, a +","+ b +"\n" ); 
	print(infoFile, "# date\n");
	print(infoFile, date + "\n");
	print(infoFile, "# strain\n");
	print(infoFile, strain + "\n");
	print(infoFile, "# experiment ID\n");
	print(infoFile, experimentID + "\n");
	print(infoFile, "# movie index\n");
	print(infoFile, movieIndex + "\n");
	print(infoFile, "# cell number\n");
	print(infoFile, c + "\n");

	
	negIm = getImageID();
	setRange(a,b);
	print("nSlices = "+ nSlices); 
	roiManager("select", c-1);
	setSlice(1);
	run("Crop");
	//calc the new height and width
	hh = (getHeight() * scaleAmount);
	ww = (getWidth() * scaleAmount);

	

	//resize commmands
	resizeCommand = "width=&ww height=&hh constrain interpolation=None";
	resizeCommandInterpol = "width=&ww height=&hh constrain interpolation=Bilinear";

	if(scaleAmount != 1) run("Size...", resizeCommandInterpol); //scale neg image
	saveAs("tiff", saveDir + "/" + saveName + "_neg.tif"); // save neg image

	// save scale and frame rate to file
	getPixelSize(unit, pw, ph, pd);
	print(infoFile, "# scale\n");
	print(infoFile, pw + "\n");
	print(infoFile, "# frame Interval\n");
	print(infoFile, frameInterval + "\n");
	File.close(infoFile);
	close(); //close neg image

	// open, blur and save seg image
	open(segFile);
	segIm = getImageID();
	setRange(a,b);
	roiManager("select", c-1);
	setSlice(1);
	run("Crop");
	if(scaleAmount != 1) run("Size...", resizeCommandInterpol);
	run("Gaussian Blur...", "sigma=1.60 stack");
	saveAs("tiff", saveDir + "/" + saveName + "_seg.tif");

	if( File.exists(actinFile) ){
		open(actinFile);
		actinIm = getImageID();
		setRange(a,b);
		roiManager("select", c-1);
		setSlice(1);
		run("Crop");
		// uncomment these as not done in makebinary any more
		// contrast was stretched for whole image, not the cell
		//setMinAndMax(0, 4095);resetMinAndMax(); //make 8-bit
		run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");
		//run("8-bit");
		
		if(scaleAmount != 1) run("Size...", resizeCommand);
		saveAs("tiff", saveDir + "/" + saveName + "_act.tif");
		
		// make composite image
		selectImage(actinIm); im1 = getTitle();
		selectImage(segIm); im2 = getTitle();
		selectImage(segIm); run("Invert", "stack");
		selectImage(segIm); run("Subtract...", "value=180 stack");
		run("Merge Channels...", "red="+im2+" green=" +im1+ 
			" blue=*None* gray=*None*");
		//run("RGB Color", "slices"); // for some reason the image ID is lost here
		//selectImage("Composite");
		run("Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
		saveAs("tiff", saveDir + "/" + saveName + "_comp.tif");
		
	}
	close();
	

}
roiManager("reset");
//setBatchMode(false);

exit();

function setRange(aa,bb){
	if(aa == 1 && bb == nSlices()) return;
	aa = aa-1;
	bb = bb+1;
	n = nSlices();
	// if keeping frame 1 then don't run the remover
	// and if keeping frame nSlices don't run the remover
	//print("nSlices before remove = " + nSlices);
	if(bb < n+1) {
		print("removing "+bb+" to "+n);
		run("Slice Remover", "first="+bb+" last="+n+" increment=1");
	}
	if(aa > 0) {
		print("removing "+1+" to "+aa);
		run("Slice Remover", "first=1 last="+aa+" increment=1");
	}
	//print("nSlices after remove = " + nSlices);
}


//makeRectangle(0, 45, 366, 228);
roiManager("Reset");
roiManager("Add");



//run the crop and deinterleave
setSlice(1);
run("Crop");
run("Hyperstack to Stack");
splitCom = "how=" + numChans + " keep";
run("Deinterleave", splitCom); //split channels

//close original file
selectWindow(title);
close();

// get channel IDs
selectWindow(title + " #1");
numSlices = nSlices;
ch1_Im  = getImageID();
selectWindow(title + " #2"); //close neg stain channel
close();
if(numChans > 2){
  selectWindow(title + " #3");
  ch3_Im  = getImageID();
}

//calculate new height and width
selectImage(ch1_Im);
hh = (getHeight() * scaleAmount);
selectImage(ch1_Im);
ww = (getWidth() * scaleAmount);

//resize commmands
resizeCommand = "width=&ww height=&hh constrain interpolation=None";
resizeCommandInterpol = "width=&ww height=&hh constrain interpolation=Bilinear";

//resize fluo, save
selectImage(ch1_Im);
setMinAndMax(0, 4095);resetMinAndMax(); //make 8-bit
run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");
run("8-bit");
if(scaleAmount != 1) run("Size...", resizeCommand);
selectImage(ch1_Im);
saveAs("tiff", saveDir + "/" + saveName + "_ch1.tiff");
selectImage(ch1_Im); //close();

//save scale and frame rate to info file
getPixelSize(unit, pw, ph, pd);
print(infoFile, "# scale\n");
print(infoFile, pw + "\n");
print(infoFile, "# frame Interval\n");
print(infoFile, frameInterval + "\n");
File.close(infoFile);

// resize DIC if exists, save
if(numChans > 2){
  selectImage(ch3_Im);
  if(scaleAmount != 1) run("Size...", resizeCommandInterpol);
  selectImage(ch3_Im);
  saveAs("tiff", saveDir + "/" + movieID + "_" + cellNumber + "_ch3.tiff");
  selectImage(ch3_Im); close();
}

open(path + "/" + title + ".neg.tif");
negIm = getImageID();
roiManager("select", 0);
run("Crop");
selectImage(negIm);
if(scaleAmount != 1) run("Size...", resizeCommand);
selectImage(negIm);
saveAs("tiff", saveDir + "/" + saveName + "_neg.tif");
selectImage(negIm);
close();

open(path + "/" + title + ".seg.tif");
segIm = getImageID();
roiManager("select", 0);
run("Crop");
selectImage(segIm);
if(scaleAmount != 1) run("Size...", resizeCommand);
selectImage(segIm);run("Gaussian Blur...", "sigma=1.60 stack");
selectImage(segIm);saveAs("tiff", saveDir + "/" + saveName + "_seg.tif");
//close();

// make compoiste image
selectImage(ch1_Im); im1 = getTitle();
selectImage(segIm); im2 = getTitle();
selectImage(segIm); run("Invert", "stack");
selectImage(segIm); run("Subtract...", "value=180 stack");
run("Merge Channels...", "red="+im2+" green=" +im1+ 
	" blue=*None* gray=*None* create");
run("RGB Color", "slices");
run("Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
saveAs("tiff", saveDir + "/" + saveName + "_comp.tiff");


//imageCalculator("Subtract create stack", ch2_Im ,ch1_Im); //subtract actin from stain

while (nImages>0) { 
    selectImage(nImages); 
    //close(); 
} 

roiManager("Reset");

}
