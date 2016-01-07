macro "compileCell" {
	
//makeRectangle(0, 45, 366, 228);
roiManager("Reset");
roiManager("Add");

movieID = "171110mlcE-KO";
strain = "mlcE-KO";
date = "17,11,10";
cellNumber = "1";
movieIndex = "13";
experimentID = "2";
frameInterval = "0.22";

numChans =2;
scaleAmount = 2;


//get file title and make save directory
orginalIm  = getImageID();
path = getDirectory("image");
title = getTitle();
saveName = ""+ movieID + "_" + movieIndex + "_" + cellNumber;
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
print(infoFile, "# date\n");
print(infoFile, date + "\n");
print(infoFile, "# strain\n");
print(infoFile, strain + "\n");
print(infoFile, "# experiment ID\n");
print(infoFile, experimentID + "\n");
print(infoFile, "# movie index\n");
print(infoFile, movieIndex + "\n");
print(infoFile, "# cell number\n");
print(infoFile, cellNumber + "\n");




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
