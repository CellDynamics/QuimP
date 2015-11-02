

selectWindow("DocA Up B Dw stimu-1_p.tif");
Ia = getImageID();

selectWindow("DocA Up B Dw stimu-2_p.tif");
Ib = getImageID();

newImage("reg_DocA Up B Dw stimu-2_p", "8-bit Black", 176, 155, 235);
IbReg = getImageID();

setBatchMode(true)

newImage("reg", "8-bit Black", 176, 155, 2);
Ireg = getImageID();

for(i=1; i<=nSlices; i++){
	selectImage(Ia);
	setSlice(i);
	run("Copy");

	selectImage(Ireg);
	setSlice(1);
	run("Paste");

	selectImage(Ib);
	setSlice(i);
	run("Copy");

	selectImage(Ireg);
	setSlice(2);
	run("Paste");

	setSlice(1);
	run("StackReg", "transformation=Translation");
	setSlice(2);
	run("Copy");

	selectImage(IbReg);
	setSlice(i);
	run("Paste");

}
setBatchMode(false)

selectImage(Ireg);
setBatchMode(false);



