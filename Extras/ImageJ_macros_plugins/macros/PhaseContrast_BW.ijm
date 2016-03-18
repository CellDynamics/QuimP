
originalStackName= getTitle();
orgID = getImageID();

// remove!
//if (File.exists (getDirectory("temp")+"QuimP_PS_Params.txt") ==1) {
	//File.delete(getDirectory("temp")+"QuimP_PS_Params.txt");	
//}

// get parameters from temp file 
if (File.exists (getDirectory("temp")+"QuimP_PS_Params.txt") ==1) {
	oldParameters=File.openAsString(getDirectory("temp")+"QuimP_PS_Params.txt");
	parametersArray=split(oldParameters, "\n");	
	default1=parametersArray[1];
	default2=parametersArray[2];
	default3=parametersArray[3];	
	default4=parametersArray[4];	
} else {	
	default1= 3;
	default2= 20;
	default3= 30;	
	default4= 1;		
}
	
Dialog.create("Phase contrast to black/white filter parameters");

Dialog.addNumber("Median Light iterations (noise smoothing)", default1);
Dialog.addNumber("Diffusion itterations (noise smoothing)", default2);
Dialog.addNumber("Rolling ball size for background subtraction", default3);
Dialog.addCheckbox("Invert (if light background)", default4);

Dialog.show();	//display dialog

medianIterations = Dialog.getNumber();
diffInts = Dialog.getNumber();
rollingSize = Dialog.getNumber();
doInvert = Dialog.getCheckbox(); 

run("8-bit");

//de-specal- light noise
setBatchMode(true);	
for (iteration=1; iteration <=medianIterations; iteration++) {	//adaptive median iterations
	for (n=1; n<=nSlices; n++) {
		setSlice(n);	
		run("Median Light");	
	}
	
}
	
for (n=1; n<=nSlices; n++) {
	setSlice(n);	
	run("Inhomogen Isotropic Diffusion 2D", "lambda=10 iterations=&diffInts dt=0.10");
}
setBatchMode(false);
	
setSlice(1);

run("Subtract Background...", "rolling=&rollingSize light stack");
if(doInvert == 1) run("Invert", "stack");
run("Enhance Contrast", "saturated=0.01 normalize_all"); //run("Enhance Contrast", "saturated=0.01 normalize normalize_all use");


if (File.exists (getDirectory("temp")+"QuimP_PS_Params.txt") ==1) {
	File.delete(getDirectory("temp")+"QuimP_PS_Params.txt");	
}
parameterFile=File.open(getDirectory("temp")+"QuimP_PS_Params.txt");
print(parameterFile,"QuimP11 Phase contrast to black/white filter: Last parameters used");
print(parameterFile, medianIterations+"\n"+diffInts+"\n"+rollingSize+"\n"+doInvert);
File.close(parameterFile);

selectImage(orgID);

if(indexOf(originalStackName, ".") != -1 ) { 	//if the file name contains a dot, make sure the extension doesn't get lost
	fileExtension= substring(originalStackName, lastIndexOf(originalStackName,"."), lengthOf(originalStackName));
	finalStackName= replace(originalStackName, fileExtension , "_BW");	
	finalStackName = finalStackName + fileExtension;	//rename the file by including "black and white"
	rename(finalStackName);
} else {
	rename(originalStackName+"_BW");	
}



