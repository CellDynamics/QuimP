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

}macro "draw best fit elipse"{

// This macro draws the best fit ellipse for the current area selection.
// It uses the List.setMeasurements() and List.getValue() functions that
// were added to the macro language in ImageJ 1.42i.
  
  requires("1.42i");
  if (selectionType==-1)
      exit("Area selection required");
  List.setMeasurements;
  //print(List.getList); // list all measurements
  x = List.getValue("X");
  y = List.getValue("Y");
  a = List.getValue("Major");
  b = List.getValue("Minor");
  angle = List.getValue("Angle");
  getVoxelSize(w, h, d, unit);
  drawEllipse(x/w, y/w, (a/w)/2, (b/w)/2, angle);
  exit;

  function drawEllipse(x, y, a, b, angle) {
      autoUpdate(false);
      setLineWidth(2);
      beta = -angle * (PI/180);
      for (i=0; i<=360; i+=2) {
          alpha = i*(PI/180) ;
          X = x + a*cos(alpha)*cos(beta) - b*sin(alpha)*sin(beta);
          Y = y + a*cos(alpha)*sin(beta) + b*sin(alpha)*cos(beta);
          if (i==0) moveTo(X, Y); else lineTo(X,Y);
          if (i==0) {ax1=X; ay1=Y;}
          if (i==90) {bx1=X; by1=Y;}
          if (i==180) {ax2=X; ay2=Y;}
          if (i==270) {bx2=X; by2=Y;}
      }
      drawLine(ax1, ay1, ax2, ay2);
      drawLine(bx1, by1, bx2, by2);
      updateDisplay;
  }

}macro "backgouroundSubtraction"{

roiManager("reset") 
roiManager("Add");
for (i=1;i<=nSlices;i++) { 
	setSlice(i); 
	getStatistics(area, mean, min, max, std, histogram); 
	run("Select All"); 
	run("Subtract...", "value="+mean); 
	roiManager("select", 0); 
}

}macro 'blebKymo'{

	i = getSliceNumber();
	numFrames = i+18;

	for( ; i <= numFrames ;){
		setSlice(i);
		
		i++;
	}


}macro "compileCell" {
	
makeRectangle(0, 45, 366, 228);
roiManager("Reset");
roiManager("Add");


cellNumber = "1";
movieIndex = "5";
date = "171110";
numChans =2;
scaleAmount = 1;


//get file title and make save directory
orginalIm  = getImageID();
path = getDirectory("image");
title = getTitle();
saveName = ""+ date + "_" + movieIndex + "_" + cellNumber;
saveDir = path + "/" + saveName;
if(File.exists(saveDir)==1){
	exit("Folder " + saveDir + " already exists");
}
File.makeDirectory(saveDir);


// save crop location etc
infoFile = File.open(saveDir +"/"+saveName + "_info.txt");
print(infoFile,"#source file\n");
print(infoFile,title+"\n");
getSelectionBounds(x,y,w,h);
print(infoFile, "#crop location (x,y,width,height)\n");
print(infoFile, "#makeRectangle(" + x+","+y+","+w+","+h+')');
print(infoFile, x+","+y+","+w+","+h+"\n");
File.close(infoFile);

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

// resize DIC if exists, save
if(numChans > 2){
  selectImage(ch3_Im);
  if(scaleAmount != 1) run("Size...", resizeCommandInterpol);
  selectImage(ch3_Im);
  saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_ch3.tiff");
  selectImage(ch3_Im); close();
}

open(path + "/" + title + ".neg.tif");
negIm = getImageID();
roiManager("select", 0);
run("Crop");
selectImage(negIm);
saveAs("tiff", saveDir + "/" + saveName + "_neg.tif");
close();

open(path + "/" + title + ".seg.tif");
segIm = getImageID();
roiManager("select", 0);
run("Crop");
selectImage(segIm);
saveAs("tiff", saveDir + "/" + saveName + "_seg.tif");
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

selectImage(segIm);
run("Gaussian Blur...", "sigma=1.60 stack");


//imageCalculator("Subtract create stack", ch2_Im ,ch1_Im); //subtract actin from stain

while (nImages>0) { 
    selectImage(nImages); 
    //close(); 
} 


}
macro "correctInvert"{



setSlice(1);

for( i=1; i<=nSlices; i++){

  setSlice(i);	
  getRawStatistics(nPixels, mean, min, max, std, histogram);
  
  if(mean > 100){ //if white background 	
  	run("Invert", "slice");
  }
}

}macro 'Manual drift Correction' { 
   extraspace = 5; //padding the image at all sides by this value (pixels) 

   var leftClick=16, rightClick=4; 
   var shift=1, control=2, alt=8; 

   requires("1.42j"); 
   n=nSlices(); 
   if (n<2) exit ("stack required"); 
   xs=newArray(nSlices); 
   ys=newArray(nSlices); 
   for (i=0; i<n; i++) 
     xs[i] = -1; //initialize positions as unset 

   nClicked = 0; 
   i=getSliceNumber()-1; 
   flags=0; 
   lasti = -1; 
   posS = ""; 
   while (!isKeyDown("space")) { 
     getCursorLoc(x, y, z, flags); 
     if (flags&leftClick !=0) { 
       makePoint(x,y); 
       xs[i] = x; 
       ys[i] = y; 
       nClicked++; 
       lasti = i; 
       do { 
         wait(50); 
         getCursorLoc(x, y, z, flags); 
       } while (flags&leftClick !=0); 
     } 
     if (i != z) {     //slice changed 
       i = z; 
       if (xs[i]>=0) { 
         makePoint(xs[i],ys[i]); 
         lasti = i; 
       } 
       while (isKeyDown("alt")) wait(50); 
     } 
     wait(20); 
     if (lasti>=0) posS = "pos. from "+(lasti+1); 
     showStatus("','=prev, '.'=next, space=done, esc=abort. "+posS); 
   } 
   if (nClicked<2) exit("Abort - at least two positions required"); 
   lasti = -1; 
   for (i=0; i<n; i++) { 
     if (xs[i]>=0) { 
       if (lasti<0) { 
         lastx=xs[i]; 
         lasty=ys[i]; 
       } 
       for (j=lasti+1; j<i; j++) {  //interpolate 
         xs[j] = lastx + (xs[i]-lastx)*(j-lasti)/(i-lasti); 
         ys[j] = lasty + (ys[i]-lasty)*(j-lasti)/(i-lasti); 
       } 
       lastx=xs[i]; 
       lasty=ys[i]; 
       lasti=i; 
     } 
   } 
   if (lasti<n-1) { 
     for (j=lasti+1; j<n; j++) { 
       xs[j] = lastx; 
       ys[j] = lasty; 
     } 
   } 
   Array.getStatistics(xs, xmin, xmax, mean, stdDev); 
   Array.getStatistics(ys, ymin, ymax, mean, stdDev); 
   newwidth = getWidth()+(xmax-xmin)+2*extraspace; 
   newheight = getHeight() +(ymax-ymin)+2*extraspace; 
   run("Canvas Size...", "width="+ newwidth +" height="+ newheight +" position=Top-Left zero"); 
   for (i=0; i<n; i++) { 
     setSlice(i+1); 
     run("Translate...", "x="+(xmax-xs[i]+extraspace)+" y="+(ymax-ys[i]+extraspace)+" slice"); 
   } 
   showStatus("stack registered"); 

} 

macro "Gaussian Fit" {
// get line info
getLine(x1, y1, x2, y2, lineWidth);
   if (x1==-1)
      exit("This macro requires a straight line selection");

intensities = getProfile();

min = mMin(intensities);
max = mMax(intensities);

// invert the intensities for gaussian fitting
for(i=0;i<lengthOf(intensities);i++){
	intensities[i] = (min - (intensities[i] - min)) + max;
}

// array 1,2,3...n
pixels = newArray(intensities.length);
  for (i=0; i<pixels.length; i++)
      pixels[i] = i;

//Plot.create("inverted intensities","pixel","intensity prime ", pixels, intensities);

// fit gaussian, get centre
Fit.doFit("Gaussian",pixels, intensities);
Fit.plot();

slice = getSliceNumber();

// check the quality of the fit
if(Fit.rSquared < 0.5){
	if(slice<nSlices) 
		setSlice(slice+1);
	exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
}
centre = Fit.p(2);

//calc the pixel position of the centre, follow a vector from x1,y1
Vx = x2-x1;
Vy = y2-y1;
norm = sqrt( (Vx*Vx) + (Vy*Vy) );
Ux = Vx / norm;
Uy = Vy / norm;

Cx = x1 + (Ux * centre);
Cy = y1 + (Uy * centre);

print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

// output to file (read in whole file and re-write)
STRING = File.openAsString("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
F = File.open("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
print(F,STRING);
print(F, ""+ Cx +" " + Cy + " "+ slice);

//draw centre
setColor("red");
drawCOval(Cx,Cy, 7,7);

// go to next slice
if(slice<nSlices) 
	setSlice(slice+1);

//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}



}




macro "Gaussian Fit [f3]" {

//open file for writing
STRING = File.openAsString("./OFI_tmp.txt");
F = File.open("./OFI_tmp.txt");
print(F,STRING);

// get line info
getLine(x1, y1, x2, y2, lineWidth);
   if (x1==-1)
      exit("This macro requires a straight line selection");

intensities = getProfile();

min = mMin(intensities);
max = mMax(intensities);

// invert the intensities for gaussian fitting
for(i=0;i<lengthOf(intensities);i++){
	intensities[i] = (min - (intensities[i] - min)) + max;
}

// array 1,2,3...n
pixels = newArray(intensities.length);
  for (i=0; i<pixels.length; i++)
      pixels[i] = i;

//Plot.create("inverted intensities","pixel","intensity prime ", pixels, intensities);

// fit gaussian, get centre
Fit.doFit("Gaussian",pixels, intensities);
//Fit.plot();

slice = getSliceNumber();
cResults = nResults(); // current result

// check the quality of the fit
if(Fit.rSquared < 0.5){
	if(slice<nSlices) 
		setSlice(slice+1);
	
	setResult("X",cResults, -1);
	setResult("Y", cResults, -1);
	setResult("Slice",cResults,slice);
      updateResults();
	exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
}
centre = Fit.p(2);

//calc the pixel position of the centre, follow a vector from x1,y1
Vx = x2-x1;
Vy = y2-y1;
norm = sqrt( (Vx*Vx) + (Vy*Vy) );
Ux = Vx / norm;
Uy = Vy / norm;

Cx = x1 + (Ux * centre);
Cy = y1 + (Uy * centre);

//print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

//Print to results window
setResult("X",cResults, Cx);
setResult("Y", cResults, Cy);
setResult("Slice",cResults,slice);
updateResults();

// output to file (read in whole file and re-write)
print(F, ""+ Cx +" " + Cy + " "+ slice);

//draw centre
setColor("red");
drawCOval(Cx,Cy, 7,7);

// go to next slice
if(slice<nSlices) 
	setSlice(slice+1);

//-------------------------------------------------------------

//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}



}




macro "GetCell [n0]" {
	
//makeRectangle(229, 120, 233, 373);

cellNumber = "c5";
date = "051710";
numChans =3;
scaleAmount = 1;


//get file title and make save directory
orginalIm  = getImageID();
path = getDirectory("image");
title = getTitle();
saveDir = path + "/" + date + "_" + cellNumber;
if(File.exists(saveDir)==1){
	exit("Folder " + saveDir + " already exists");
}
File.makeDirectory(saveDir);

// save crop location etc
infoFile = File.open(saveDir + "/info.txt");
print(infoFile,"#source file\n");
print(infoFile,title+"\n");
getSelectionBounds(x,y,w,h);
print(infoFile, "#crop location (x,y,width,height)\n");
print(infoFile, x+","+y+","+w+","+h+"\n");
File.close(infoFile);

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
selectWindow(title + " #2");
ch2_Im  = getImageID();
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
if(scaleAmount != 1) run("Size...", resizeCommand);
selectImage(ch1_Im);
saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_ch1.tiff");
//selectImage(ch1_Im); close();

// resize neg stain, save
selectImage(ch2_Im);
if(scaleAmount != 1) run("Size...", resizeCommandInterpol);
selectImage(ch2_Im);
saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_ch2.tiff");

// make seg image. blur, binary, invert, blur, save
selectImage(ch2_Im); run("Gaussian Blur...", "sigma=1 stack");
selectImage(ch2_Im); run("Make Binary", "calculate black");
//selectImage(ch2_Im); run("Invert", "stack");
//selectImage(ch2_Im); run("Fill Holes", "stack");
selectImage(ch2_Im); run("Gaussian Blur...", "sigma=1.2 stack");
selectImage(ch2_Im);
saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_seg.tiff");


// resize DIC if exists, save
if(numChans > 2){
  selectImage(ch3_Im);
  if(scaleAmount != 1) run("Size...", resizeCommandInterpol);
  selectImage(ch3_Im);
  saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_ch3.tiff");
  selectImage(ch3_Im); close();
}

// make compoiste image
selectImage(ch1_Im); im1 = getTitle();
selectImage(ch2_Im); im2 = getTitle();
selectImage(ch2_Im); run("Invert", "stack");
selectImage(ch2_Im); run("Subtract...", "value=180 stack");
run("Merge Channels...", "red="+im2+" green=" +im1+ 
	" blue=*None* gray=*None* create");
run("RGB Color", "slices");
run("Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
saveAs("tiff", saveDir + "/" + date + "_" + cellNumber + "_comp.tiff");


//imageCalculator("Subtract create stack", ch2_Im ,ch1_Im); //subtract actin from stain

while (nImages>0) { 
    selectImage(nImages); 
    close(); 
} 




}

macro "Gaussian Fit [f1]" {
// get line info
getLine(x1, y1, x2, y2, lineWidth);
   if (x1==-1)
      exit("This macro requires a straight line selection");

intensities = getProfile();

min = mMin(intensities);
max = mMax(intensities);

// invert the intensities for gaussian fitting
for(i=0;i<lengthOf(intensities);i++){
	intensities[i] = (min - (intensities[i] - min)) + max;
}

// array 1,2,3...n
pixels = newArray(intensities.length);
  for (i=0; i<pixels.length; i++)
      pixels[i] = i;

//Plot.create("inverted intensities","pixel","intensity prime ", pixels, intensities);

// fit gaussian, get centre
Fit.doFit("Gaussian",pixels, intensities);
Fit.plot();

slice = getSliceNumber();

// check the quality of the fit
if(Fit.rSquared < 0.5){
	if(slice<nSlices) 
		setSlice(slice+1);
	exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
}
centre = Fit.p(2);

//calc the pixel position of the centre, follow a vector from x1,y1
Vx = x2-x1;
Vy = y2-y1;
norm = sqrt( (Vx*Vx) + (Vy*Vy) );
Ux = Vx / norm;
Uy = Vy / norm;

Cx = x1 + (Ux * centre);
Cy = y1 + (Uy * centre);

print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

// output to file (read in whole file and re-write)
STRING = File.openAsString("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
F = File.open("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
print(F,STRING);
print(F, ""+ Cx +" " + Cy + " "+ slice);

//draw centre
setColor("red");
drawCOval(Cx,Cy, 7,7);

// go to next slice
if(slice<nSlices) 
	setSlice(slice+1);

//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}



}




macro "nextSliceSelect [n2]"{
setSlice(getSliceNumber()+1);
run("Select None");
}macro "BK subtraction [n1]"{

roiManager("reset") 
roiManager("Add");

//orginalIm  = getImageID();
//path = getDirectory("image");
//title = getTitle();

// subtract mean background from each frame
for (i=1;i<=nSlices;i++) { 
	setSlice(i); 
	getStatistics(area, mean, min, max, std, histogram); 
	run("Select All"); 
	run("Subtract...", "value="+mean); 
	roiManager("select", 0); 
}

roiManager("reset");
run("Select All"); 
// normalize whole image
run("Enhance Contrast", "saturated=0.001 normalize use");


//saveAs("tiff", path+"/"+title+"_norm.tif");
}macro "parallel gaussian [f2]"{
	nLines = 5; //num parallel lines to sample from eith side of user line
	maxDisp = 1.3;
	sensativity = 0.3;

	disp = (maxDisp * 2) / (nLines-1);
	
	slice = getSliceNumber();
	getLine(x1, y1, x2, y2, lineWidth);
  	if (x1==-1)
		exit("This macro requires a straight line selection");
	
	length = getProfile();
	length = length.length;
	intensities = newArray(length);

	// calc unit vector from x1 y1
	U = unitVec(x1,y1,x2,y2);

	//calc vec at 90deg
	tmpUx = (U[0] * cos(PI/2)) - (U[1] * sin(PI/2));
	Uy = (U[1] * cos(PI/2)) + (U[0] * sin(PI/2));
	Ux = tmpUx;

	// draw and retrive intensities from parallel lines
	for(i= -maxDisp; i <=maxDisp; i+=disp){
		//print("i = " + i);
		parCoor = getParCoor(Ux,Uy, i);
		makeLine(parCoor[0], parCoor[1],parCoor[2],parCoor[3]);
		parIntensities = getProfile();
		
		for(j=0;j<intensities.length;j++){
			intensities[j] = intensities[j]+parIntensities[j];
		}
	}

	// average intensities
	for(j=0;j<intensities.length;j++){
		intensities[i] = intensities[i] / nLines;
	}

	min = mMin(intensities);
	max = mMax(intensities);

	// invert the intensities for gaussian fitting
	for(i=0;i<intensities.length;i++){
		intensities[i] = (min - (intensities[i] - min)) + max;
	}

	// fit gaussian, get centre

	// array 1,2,3...n
	pixels = newArray(intensities.length);
  	for (i=0; i<pixels.length; i++)
      		pixels[i] = i;

	Fit.doFit("Gaussian",pixels, intensities);
	//Fit.plot();

	// check the quality of the fit
	if(Fit.rSquared < sensativity){
		//if(slice<nSlices) 
			//setSlice(slice+1);
		//exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
	}
	centre = Fit.p(2);

	//calc the pixel position of the centre, follow a vector from x1,y1
	Cx = x1 + (U[0] * centre);
	Cy = y1 + (U[1] * centre);

	print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

	//draw centre
	setColor("white");
	drawLine(Cx, Cy, Cx, Cy);

	// output to file (read in whole file and re-write)
	title = getTitle();
	location = "/Users/Richard/Documents/PhD/data/bleachCoor/" + title + "_BT.txt";
	print(location);

	if( ! File.exists(location) ){
		print("nine");
		ff = File.open(location);
		print(ff, ""+ Cx +" " + Cy + " "+ slice);
		File.close(ff);
	}else{
		STRING = File.openAsString(location);
		F = File.open(location);
		print(F,STRING);
		print(F, ""+ Cx +" " + Cy + " "+ slice);
	}
	// go to next slice
	if(slice<nSlices) 
		setSlice(slice+1);

}

//--------------------------------------------------------

function getParCoor(Ux,Uy,disp){
	//get coor for a line parrellel to the original line
	coor = newArray(4);
	coor[0] = x1 + ( Ux* disp);
	coor[2] = x2 + ( Ux* disp);
	coor[1] = y1 + ( Uy* disp);
	coor[3] = y2 + ( Uy* disp);
	return coor;
}


//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}


function unitVec(x1,y1,x2,y2){
	U = newArray(2);
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	U[0] = Vx / norm;
	U[1] = Vy / norm;
	
	return U;
}






macro "parallel gaussian"{
	nLines = 2; //num parallel lines to sample from eith side of user line
	disp = 10;
	xC = newArray((nLines*2) +1);
	yC = newArray((nLines*2) +1);

	getLine(x1, y1, x2, y2, lineWidth);
  	if (x1==-1)
		exit("This macro requires a straight line selection");

	centre = calcCentre();
	xC[0] = centre[0];
	yC[0] = centre[1];
	print(xC[0] +" " + yC[0] + " " + PI);

	// calc unit vector from x1 y1
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	Ux = Vx / norm;
	Uy = Vy / norm;

	//turn 90deg
	tmpUx = (Ux * cos(PI/2)) - (Uy * sin(PI/2));
	Uy = (Uy * cos(PI/2)) + (Ux * sin(PI/2));
	Ux = tmpUx;


	for(i=1;i<nLines;i++){
		x1n = x1 + ( Ux* (disp*i));
		x2n = x2 + ( Ux* (disp*i));
		y1n = y1 + ( Uy* (disp*i));
		y2n = y2 + ( Uy* (disp*i));
		makeLine(x1n,y1n,x2n,y2n);
		centre = calcCentre();
		xC[i] = centre[0];
		yC[i] = centre[1];
		print(xC[i] +" " + yC[i] + " " + PI);
	}

}

//----------------------------------------------------


function calcCentre(){
	intensities = getProfile();
	min = mMin(intensities);
	max = mMax(intensities);

	// invert the intensities for gaussian fitting
	for(i=0;i<lengthOf(intensities);i++){
		intensities[i] = (min - (intensities[i] - min)) + max;
	}

	// array 1,2,3...n
	pixels = newArray(intensities.length);
 	for (i=0; i<pixels.length; i++)
      		pixels[i] = i;
	
	// fit gaussian, get centre
	Fit.doFit("Gaussian",pixels, intensities);
	//Fit.plot();

	//cheack quality of the fit
	if(Fit.rSquared < 0.5){
		print("Fit to gaussian is too weak (" + Fit.rSquared + ")");
		fail = newArray(-1,-1);
		return fail;
	}
	centre = Fit.p(2);

	//calc the pixel position of the centre, follow a vector from x1,y1
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	Ux = Vx / norm;
	Uy = Vy / norm;

	globalCentre = newArray(2);
	globalCentre[0] = x1 + (Ux * centre);
	globalCentre[1] = y1 + (Uy * centre);

	//draw centre
	setColor("red");
	drawCOval(globalCentre[0],globalCentre[1], 3,3);

	return globalCentre;
}


//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}




macro "peak finder" {
print("\\Clear")
selectWindow("GFP3_ch00.tif");
//makeLine(258, 221, 468, 374);
elipseSel();

profile = getProfile();
print("p: "+profile[200]);

preakWidth = 12;

//for (i=0; i<profile.length; i++){
	//if( profile[i] > max) max = profile[i];
	
//}

//print(max);

peaks = newArray(profile.length);
index = newArray(profile.length);
for (i=preakWidth; i<profile.length-preakWidth; i++){
     peaks[i] = 0;
	index[i] = i;
	flag = 0;
	for (j=-preakWidth; j<=preakWidth; j++){
		if(profile[i+j] > profile[i]){
			flag = 1;
		}
	}
	if(flag==0) peaks[i] = profile[i];
}

//peaks[258] = 100;
Plot.create("peaks", "pixel","intensity", index, peaks);
Plot.setLimits(0, 258, 0, 150);
Plot.show();

    
function elipseSel(){

	List.setMeasurements;
	x = List.getValue("X");
  	y = List.getValue("Y");
  	a = List.getValue("Major");
  	b = List.getValue("Minor");
  	angle = List.getValue("Angle");
  	getVoxelSize(w, h, d, unit);
	drawEllipse(x/w, y/w, (a/w)/2, (b/w)/2, angle);
	
   
}

 function drawEllipse(x, y, a, b, angle) {
 
      beta = -angle * (PI/180);
      for (i=0; i<=360; i+=2) {
          alpha = i*(PI/180) ;
          X = x + a*cos(alpha)*cos(beta) - b*sin(alpha)*sin(beta);
          Y = y + a*cos(alpha)*sin(beta) + b*sin(alpha)*cos(beta);
          
          if (i==0) {ax1=X; ay1=Y;}
          if (i==90) {bx1=X; by1=Y;}
          if (i==180) {ax2=X; ay2=Y;}
          if (i==270) {bx2=X; by2=Y;}
      }
      makeLine(ax1, ay1, ax2, ay2);
  }

}

setSlice(1);
run("Delete Slice");


level = 683;
width = 135;
stretch = 45;

max = 4095;

//scale = level / (level - crop);
scale = (width+stretch)/width;
sep = 0;

getDimensions(width, height, channels, slices, frames);


for(px = 0; px <width; px++){
	for(py = 0; py <height; py++){
		value = getPixel(px, py);
		if(value > level){
			sep =  value - level;		
			value =  level + round(sep * scale);
			if(value < 0) value = 0;
			setPixel(px, py, value);
		}else if(value < level){
			sep =  level-value;		
			value =  level - round(sep * scale);
			if(value > max) value = max;
			setPixel(px, py, value);
		}
			
	}
}


//makeRectangle(354, 0, 318, 512);

//makeRectangle(0, 0, 354, 512);
macro "setSliceLable" {

for (i=1; i<=nSlices; i++) { 
	setSlice(i);
	string = d2s(i,0) + "\n";
	setMetadata(string);
}

}