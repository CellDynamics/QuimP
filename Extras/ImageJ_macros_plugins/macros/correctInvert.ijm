macro "correctInvert"{

print("Correct invert\n");
roiManager("Reset");
roiManager("Add");

run("Select None");
setSlice(1);
roiManager("select", 0);

i=1;
for( i=1; i<=nSlices; i++){

  setSlice(i);	
  getRawStatistics(nPixels, mean, min, max, std, histogram);

  if(i == 186) print( "mean frame " + i +" = "  + mean);
  if(mean > 100){ //if white background 
  	run("Select None");	
  	run("Invert", "slice");
  	print("inverted slice" + i);
  	roiManager("select", 0);
  }
}

}