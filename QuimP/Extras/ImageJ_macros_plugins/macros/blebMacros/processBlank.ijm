
orgIm = getImageID();

//saveFile = getInfo("image.directory") + "../blank.tif";
//print("save to " + saveFile);

//setSlice(1);
//run("Delete Slice"); //delete slice 1
setBatchMode(true);  //correct intensity fluctualtions across frames
run("Select All");
run("Run...", "run=[/home/richardtyson/Documents/macros/correctIntensityFluc.ijm]"); 
setBatchMode(false);

x = nSlices; // create an average z stack
run("Z Project...", "start=1 stop=&x projection=[Average Intensity]");

blank = getImageID();  
run("Gaussian Blur...", "sigma=1"); // apply a small but to remove some noise
//saveAs("Tiff", saveFile); //saves
//close();

//selectImage(orgIm); close();



