i = getSliceNumber();

for (;i<=nSlices;i++) {
	setSlice(i);
	run("Paste");
	if(i == 42){
		//i = 99999;
	}
}