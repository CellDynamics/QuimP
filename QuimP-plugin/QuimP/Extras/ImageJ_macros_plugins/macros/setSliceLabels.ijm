macro "setSliceLable" {

for (i=1; i<=nSlices; i++) { 
	setSlice(i);
	string = d2s(i,0) + "\n";
	setMetadata(string);
}

}