
newImage("p1", "8-bit White", 400, 400, 10);
newImage("p2", "8-bit White", 400, 400, 10);
run("Merge Channels...", "red=p1 green=p2 blue=*None* gray=*None*");

exit();
id = getImageID();
			
run("RGB Color", "slices"); //image ID is lost

selectImage(id);