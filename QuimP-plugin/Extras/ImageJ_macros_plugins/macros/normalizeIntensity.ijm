macro "BK subtraction [n1]"{

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
//run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");
run("Enhance Contrast", "saturated=0.05 normalize");

//saveAs("tiff", path+"/"+title+"_norm.tif");
}