
run("Duplicate...", "title=t");
run("Make Binary");
roiManager("reset");
run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing clear add");


for(i=0; i < roiManager("count"); i++){
	roiManager("select", i);
	getRawStatistics(nPixels, mean, min, max, std, histogram);
	if(nPixels < 200){
		run("Fill", "slice");
	}
	
}
roiManager("reset");

//run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing clear add");