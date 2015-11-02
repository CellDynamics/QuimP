

saveDir = getInfo("image.directory");
tmp = substring(saveDir, 1,lastIndexOf(saveDir, "/")); //strip tailing /
saveName = substring(tmp, lastIndexOf(tmp, "/")+1);
print(saveDir +"\n"+"saveName="+saveName);

run("BOA");
waitForUser("ive finished Boa");
selectImage("Cell contours");
saveAs("tiff", saveDir + "CellContours.tif");
close();
close();

run("ECMM Mapping");
waitForUser("ive finished ecmm");
selectImage("ECMM Mappings");
saveAs("tiff", saveDir + "ECMMmappings.tif");
close();

if( File.exists(saveDir+saveName+"_act.tif" )){
	open(saveDir+saveName+"_act.tif");
	run("8-bit");
}else{
	open(saveDir+saveName+"_neg.tif"); 
}
run("ANA");
waitForUser("ive finished ana");
close();

//saveDir = "/home/richardtyson/Documents/blebbing/blebdata/171110PI3K1-5-KO_15_1/";
//saveName = "171110PI3K1-5-KO_15_1";

run("Q Analysis");
waitForUser("ive finished Q");
selectImage("Fluorescence_Map");
//saveAs("tiff", saveDir + "Fluorescence_Map");
close();

selectImage("Convexity_map");
//saveAs("tiff", saveDir + "Convexity_Map");
close();

selectImage("Motility_map");
//saveAs("tiff", saveDir + "Motility_Map");
close();







