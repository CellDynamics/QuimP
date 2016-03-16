//make a rspinning disk neg stain binary
//setBatchMode(true);  //correct intensity fluctualtions across frames
//open("/home/kay_lab/Ax2-vs-mlcE-4-5fps-100sec-013_um_per_pixel/Ax2/171110-spinning-disc-ABD-and-mlc#10.tif");
//makeRectangle(495, 196, 123, 85);

if (selectionType()==-1){ exit("No selection"); }
roiManager("reset") 
roiManager("Add");
setSlice(1);

//setBatchMode(true);

nBins = 819;
disThreshold = 10;

blankFile = "blank.tif";
orgIm = getImageID();
openDir = getInfo("image.directory");
fileName = getInfo("image.filename");
print("\\Clear");

// get all da channels
getDimensions(width, height, channels, slices, frames);
run("Split Channels");

if(channels == 1){
	// just neg stain present
	selectImage( "C1-" + fileName);
	negIm = getImageID();
	print('One channel');
}else if(channels == 2){
	print('Two channels');
	// both actin and neg present
	selectImage( "C2-" + fileName);
	negIm = getImageID();
	selectImage( "C1-" + fileName);
	actIm = getImageID();
	roiManager("select", 0);
	
	// process channel 1 - actin channel (only if more than one channel, else its the neg stain)
	// done in compile cells
	//setMinAndMax(0, 4095);resetMinAndMax(); //make 8-bit
	//run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");
	//run("8-bit");
	
	// do a background subtraction
	run("Run...", "run=[/home/richardtyson/Documents/macros/backGroundSub.ijm]"); 
	print("saved actin to "+openDir + fileName + ".act.tif" );
	saveAs("tiff", openDir + fileName + ".act.tif");
	close();
}

// correct intensities across frames in neg image
selectImage(negIm);
roiManager("select", 0);
run("Run...", "run=[/home/richardtyson/Documents/macros/correctIntensityFluc.ijm]"); 
run("Select All");

x = nSlices; // create an average z stack
run("Z Project...", "start=1 stop=&x projection=[Average Intensity]");

zIm = getImageID();

open(openDir + blankFile);

blankIm = getImageID();
roiManager("select", 0);

getHistogram(valuesB, countsB, nBins, 0, 4095);
//run("Histogram", "bins=129 x_min=0 x_max=4095 y_max=Auto"); 

drBK = findDistRange(countsB,disThreshold);
print("BK range: start "+valuesB[drBK[0]] + " (" + drBK[0] + ")"+ 
  ", end " + valuesB[drBK[1]] + " (" + drBK[1] + ")");

selectImage(zIm);
roiManager("select", 0);
getHistogram(valuesZ, countsZ, nBins, 0, 4095);
//run("Histogram", "bins=129 x_min=0 x_max=4095 y_max=Auto"); 

drZ = findDistRange(countsZ,disThreshold);
print("zIm range: start "+valuesZ[drZ[0]] + " (" + drZ[0] + ")"+
  ", end " + valuesZ[drZ[1]] + " (" + drZ[1] + ")");

// caluclate amount to strectch / contract background distribution
bkWidth = valuesB[drBK[1]] - valuesB[drBK[0]];
bkCentre = valuesB[drBK[0]] + (bkWidth / 2);

zWidth = valuesZ[drZ[1] - drZ[0]];
zCentre = valuesZ[drZ[0]] + (zWidth / 2);

wDiff = zWidth - bkWidth;  //width diff
cDiff = zCentre - bkCentre; //centre diff

print("\n DIFFS wdiff: "+wDiff+", cDuff: "+ cDiff);

selectImage(blankIm);
run("Select All");
setContrast(bkCentre, bkWidth, wDiff, 4095); //shift the distribution to match

// correct mean intensities
roiManager("select", 0);
getStatistics(area, meanBK, min, max,std, histogram); 
selectImage(zIm);
roiManager("select", 0);
getStatistics(area, meanZ, min, max, std, histogram); 
close(); // close zIm
selectImage(blankIm);
run("Select All");
mDiff = meanZ - meanBK;
print("\n shifting bk mean by "+ mDiff +"\n");
run("Add...", "value=&cDiff"); //match the mean intensity
run("Invert", "stack");

// create neg image
selectImage(negIm);
run("Select All");
run("Gaussian Blur...", "sigma=1 stack");
run("Invert", "stack");
imageCalculator("subtract create 32-bit stack", negIm , blankIm);
corrNegIm = getImageID();
run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");
run("8-bit");
saveAs("tiff", openDir + fileName + ".neg.tif");
print("saved neg to "+openDir + fileName + ".neg.tif" );

// make seg image
run("Gaussian Blur...", "sigma=3 stack");
// correct the intensity across frames again
roiManager("select", 0);
run("Run...", "run=[/home/richardtyson/Documents/macros/correctIntensityFluc.ijm]");
//save negstain image


run("Select All"); 
run("Threshold...");
waitForUser("Set a threshold and Apply!!!");
run("Fill Holes", "stack");
saveAs("tiff", openDir + fileName + ".seg.tif");
print("saved seg to "+openDir + fileName + ".seg.tif" );

while (nImages>0) { 
    selectImage(nImages); 
    close(); 
} 

setBatchMode(false);

function setContrast(level, width, stretch,max){

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

}

function findDistRange(countsB, disThreshold){
  i=0;
  
for(; i < 4095; i++){
	if(countsB[i] >= disThreshold){
		//print("start at: " + i + ", count: " + countsB[i]);
		j = i+1;
		start = i;
		i = 9999;		
	}
}

if(start >= 4094){
  print("could not find start of distribution");
  start = 0;
  end = 4095;
} else{
  for(; j < 4095; j++){
    if(countsB[j] < disThreshold){
      //print("end at: " + j + ", count: " + countsB[j]);
      end = j;
      j = 9999;
    }
  }
}
distRange = newArray(start, end);

return distRange;
	
}





