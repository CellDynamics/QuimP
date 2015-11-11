//create images for segmentation from whole directory

//setBatchMode(true);

print("\\Clear");
inDir = getDirectory("Choose the Source Directory ");
saveDir = inDir;

list = getFileList(inDir); // directory list

for (f=0; f<list.length; f++){	//got through folder

	if( !File.isDirectory(inDir+list[f]) && !endsWith(list[f],".neg.tif")
		&& !endsWith(list[f],".seg.tif") && !matches(list[f],"blank.tif")
		&& !endsWith(list[f], ".done") && !endsWith(list[f],".act.tif")){

		print("processing " + list[f]);
		open(inDir + list[f]);
		
		showStatus("Waiting for selection");
		
		while( selectionType() == -1){
			//makeRectangle(519, 255, 134, 89);
			makeRectangle(6, 6, 102, 66);
			wait(20);
			waitForUser("Make a selection");
		}
		
		run("Run...", "run=[/home/richardtyson/Documents/macros/makeBinary.ijm]");
		//exit(); 
	}
	

}

//setBatchMode(false);