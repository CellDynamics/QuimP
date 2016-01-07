macro "parallel gaussian [f2]"{
	nLines = 5; //num parallel lines to sample from eith side of user line
	maxDisp = 1.3;
	sensativity = 0.3;

	disp = (maxDisp * 2) / (nLines-1);
	
	slice = getSliceNumber();
	getLine(x1, y1, x2, y2, lineWidth);
  	if (x1==-1)
		exit("This macro requires a straight line selection");
	
	length = getProfile();
	length = length.length;
	intensities = newArray(length);

	// calc unit vector from x1 y1
	U = unitVec(x1,y1,x2,y2);

	//calc vec at 90deg
	tmpUx = (U[0] * cos(PI/2)) - (U[1] * sin(PI/2));
	Uy = (U[1] * cos(PI/2)) + (U[0] * sin(PI/2));
	Ux = tmpUx;

	// draw and retrive intensities from parallel lines
	for(i= -maxDisp; i <=maxDisp; i+=disp){
		//print("i = " + i);
		parCoor = getParCoor(Ux,Uy, i);
		makeLine(parCoor[0], parCoor[1],parCoor[2],parCoor[3]);
		parIntensities = getProfile();
		
		for(j=0;j<intensities.length;j++){
			intensities[j] = intensities[j]+parIntensities[j];
		}
	}

	// average intensities
	for(j=0;j<intensities.length;j++){
		intensities[i] = intensities[i] / nLines;
	}

	min = mMin(intensities);
	max = mMax(intensities);

	// invert the intensities for gaussian fitting
	for(i=0;i<intensities.length;i++){
		intensities[i] = (min - (intensities[i] - min)) + max;
	}

	// fit gaussian, get centre

	// array 1,2,3...n
	pixels = newArray(intensities.length);
  	for (i=0; i<pixels.length; i++)
      		pixels[i] = i;

	Fit.doFit("Gaussian",pixels, intensities);
	//Fit.plot();

	// check the quality of the fit
	if(Fit.rSquared < sensativity){
		//if(slice<nSlices) 
			//setSlice(slice+1);
		//exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
	}
	centre = Fit.p(2);

	//calc the pixel position of the centre, follow a vector from x1,y1
	Cx = x1 + (U[0] * centre);
	Cy = y1 + (U[1] * centre);

	print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

	//draw centre
	setColor("white");
	drawLine(Cx, Cy, Cx, Cy);

	// output to file (read in whole file and re-write)
	title = getTitle();
	location = "/Users/Richard/Documents/PhD/data/bleachCoor/" + title + "_BT.txt";
	print(location);

	if( ! File.exists(location) ){
		print("nine");
		ff = File.open(location);
		print(ff, ""+ Cx +" " + Cy + " "+ slice);
		File.close(ff);
	}else{
		STRING = File.openAsString(location);
		F = File.open(location);
		print(F,STRING);
		print(F, ""+ Cx +" " + Cy + " "+ slice);
	}
	// go to next slice
	if(slice<nSlices) 
		setSlice(slice+1);

}

//--------------------------------------------------------

function getParCoor(Ux,Uy,disp){
	//get coor for a line parrellel to the original line
	coor = newArray(4);
	coor[0] = x1 + ( Ux* disp);
	coor[2] = x2 + ( Ux* disp);
	coor[1] = y1 + ( Uy* disp);
	coor[3] = y2 + ( Uy* disp);
	return coor;
}


//find maximum value in array
function mMax(values){
	max=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] > max) {
			max = values[i];
		}
	}
	return max;
}

// find minimum values in array
function mMin(values){
	min=values[0];
	for(i=1;i<values.length;i++){
		if (values[i] < min) {
			min = values[i];
		}
	}
	return min;
}

function drawCOval(x,y,w,h){
	x = x - (w/2);
	y = y - (h/2);
	drawOval(x,y,w,h);
}


function unitVec(x1,y1,x2,y2){
	U = newArray(2);
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	U[0] = Vx / norm;
	U[1] = Vy / norm;
	
	return U;
}






