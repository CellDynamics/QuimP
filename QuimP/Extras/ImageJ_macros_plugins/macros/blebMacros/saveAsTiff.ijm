//convert all .htm microscopy images to tiff

duration = 200; // duration of movies in seconds
pixWidth = 0.1317882;
fInterval = 0.5; //duration / frames;

inDir = getDirectory("Choose the Source Directory ");
saveDir = inDir;

list = getFileList(inDir); // directory list

for (f=0; f<list.length; f++){	//got through folder

	if( File.isDirectory(inDir+list[f])){
		
	print(list[f] + " is a directory...");
	sublist = getFileList(inDir + list[f]); // subdirectory list
	//home/kay_lab/071210-spin-disc-agar07-PI3K&PikI-KOs-100sec-013umperpixel
	for(sf = 0 ; sf<sublist.length; sf++){
		if( endsWith(sublist[sf], ".htm" )){
			fileName = replace(list[f], "/", ""); //remove trailing / (new string made)
			openFile = inDir + list[f] + sublist[sf];
			saveFile = saveDir + fileName + ".tif";

			print("Converting " + openFile);

			importCommand = "open=" + openFile + " color_mode=Default view=Hyperstack stack_order=Default";
			run("Bio-Formats Importer", importCommand);

			// remove the first blank slice
			setSlice(1);
			//run("Delete Slice");
			getDimensions(width, height, channels, slices, frames);
			frames = nSlices / channels;
			
			print("\t"+frames+"frs, set frame interval to " + fInterval);

			properties = "slices=1 frames=" +frames+" frame=["+fInterval+" sec]";
			run("Properties...", properties);
			setVoxelSize(pixWidth,pixWidth,0, "microns");

			if( matches( list[f], ".*blank.*") ){
				// process blank
				print("processing blank");
				run("Run...", "run=[/home/richardtyson/Documents/macros/processBlank.ijm]");
				properties = "slices=1 frames=1 frame=[0 sec]";
			    run("Properties...", properties);
				saveAs("Tiff", saveDir + "blank.tif"); 
			}else{
				saveAs("Tiff", saveFile);
			}
			close();
			//sf = 1e9;
		}
	}

	//f=5;
	
	//f =9999;
	}
}

