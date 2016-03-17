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
