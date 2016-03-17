run("Duplicate...", "title=05Dec2011-F01-1.tif");// a simple macro that combines two edge preserving smoothing plugins (Median Light and Inhomogen Isotropic Diffusion 2D),
// followed by background subtraction and dual value thresholding. Useful to convert phase contrast movies to black and white. 

originalStackName= getTitle();

//The user's previous choices are stored in a file called PhaseContrastParameters.txt in the temp folder;
//the defaults in the dialog are filled-in accordingly

if (File.exists (getDirectory("temp")+"PhaseContrastParameters.txt") ==1) {	//if the parameter file exists, open it
	oldParameters=File.openAsString(getDirectory("temp")+"PhaseContrastParameters.txt");
	parametersArray=split(oldParameters, "\n");	//parse data into an array
	default1=parametersArray[1];	//set the defaults according to the parameter array; skip entry 0 as it only contains a heading
	default2=parametersArray[2];
	default3=parametersArray[3];	
	default4=parametersArray[4];	
	default5=parametersArray[5];	

} else {	

	default1= 3;	//set the defaults according to the parameter array; skip entry 0 as it only contains a heading
	default2= 7;
	default3= 0;	
	default4= 30;	
	default5= 8;
}
	
Dialog.create("Phase contrast to black/white filter parameters");

Dialog.addNumber("Number of edge preserving smoothing iterations", default1);	

Dialog.addNumber("Rolling ball radius for background subtraction", default2 );
Dialog.addCheckbox("re-inquire the remaining parameters after background subtraction", default3 );
Dialog.addMessage("Dual thresholding limits:");
Dialog.addNumber("Upper threshold", default4 );
Dialog.addNumber("Lower threshold", default5 );

Dialog.show();	//display dialog

medianIterations= Dialog.getNumber();
askAgain=Dialog.getCheckbox();	
rollingBallRadius= Dialog.getNumber();		
highThreshold=Dialog.getNumber();
lowThreshold=Dialog.getNumber();	

//de-specal- light noise
for (iteration=1; iteration <=medianIterations; iteration++) {	//adaptive median iterations
	setBatchMode(true);		
	for (n=1; n<=nSlices; n++) {
		setSlice(n);	
		run("Median Light");	
	}
	setBatchMode(false);
}


	setBatchMode(true);		
	for (n=1; n<=nSlices; n++) {
		setSlice(n);	
		run("Inhomogen Isotropic Diffusion 2D", "lambda=10 iterations="+medianIterations+" dt=0.10");
	}
	setBatchMode(false);



run("Subtract Background...", "rolling="+rollingBallRadius+" light stack");

getRawStatistics(nPixels, mean, min, max, std, histogram);
if (mean> 150)
	run("Invert", "stack");

if (askAgain== 1) {

	showMessageWithCancel("Determine upper and lower threshold and press spacebar when done");	

	while (isKeyDown("space")==0) { 	//wait for spacebar to be pressed by user
		wait(20);
	}
	wait(200);
	Dialog.create("Dual threshold limits");
	Dialog.addNumber("Upper threshold", highThreshold );
	Dialog.addNumber("Lower threshold", lowThreshold );

	Dialog.show();	//display dialog

	highThreshold=Dialog.getNumber();
	lowThreshold=Dialog.getNumber();	
}
//The user's previous choices are stored in a file called PhaseContrastParameters.txt in the temp folder;

if (File.exists (getDirectory("temp")+"PhaseContrastParameters.txt") ==1) 	//if the parameter file exists, delete it
	File.delete(getDirectory("temp")+"PhaseContrastParameters.txt");	

	// (re)create parameter file; 
parameterFile=File.open(getDirectory("temp")+"PhaseContrastParameters.txt");	//name it PhaseContrastParameters.txt
print(parameterFile,"this file contains the parameters that were entered by the user the last time the macro was used");  //heading
print(parameterFile, medianIterations+"\n"+rollingBallRadius+"\n"+askAgain+"\n"+highThreshold+"\n"+lowThreshold);	
							//fill in the parameters, separated by "\n" (new line)
File.close(parameterFile);	//close the file when done




run("Hysteresis ", "high="+highThreshold+" low="+lowThreshold);
outputID= getImageID();

if ( isOpen("Trinarisation") ) {
	selectWindow("Trinarisation");
	close();
}

selectImage(outputID);

if(indexOf(originalStackName, ".") != -1 ) { 	//if the file name contains a dot, make sure the extension doesn't get lost
	fileExtension= substring(originalStackName, lastIndexOf(originalStackName,"."), lengthOf(originalStackName));
	finalStackName= replace(originalStackName, fileExtension , "_black and white");	
	finalStackName = finalStackName + fileExtension;	//rename the file by including "black and white"
	rename(finalStackName);
} else {
	rename(originalStackName+"_black and white");	
}

exit();

	


