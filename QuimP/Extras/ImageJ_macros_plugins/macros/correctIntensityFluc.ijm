
roiManager("reset") 
roiManager("Add");
getStatistics(area, mean, min, max, std, histogram); 
targetMean = mean;
roiManager("select", 0); 
roiManager("Rename", "k");

//setBatchMode(true);
  
for (i=1;i<=nSlices;i++) {
	
	run("Select None"); 
	roiManager("select", 0);
	setSlice(i);
	getStatistics(area, mean, min, max, std, histogram); 
	diff = targetMean - mean;
	print("diff:"+diff+" mean:"+ mean + " ,target:" + targetMean);
	run("Select All"); 
	run("Add...", "value="+ diff +" slice");	
}

//setBatchMode(false);