macro "backgouroundSubtraction"{

roiManager("reset") 
roiManager("Add");

setBatchMode(true);

for (i=1;i<=nSlices;i++) { 
	setSlice(i); 
	getStatistics(area, mean, min, max, std, histogram); 
	run("Select All"); 
	run("Subtract...", "value="+mean); 
	roiManager("select", 0); 
}

setBatchMode(false);

}