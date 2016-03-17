macro "parallel gaussian"{
	nLines = 2; //num parallel lines to sample from eith side of user line
	disp = 10;
	xC = newArray((nLines*2) +1);
	yC = newArray((nLines*2) +1);

	getLine(x1, y1, x2, y2, lineWidth);
  	if (x1==-1)
		exit("This macro requires a straight line selection");

	centre = calcCentre();
	xC[0] = centre[0];
	yC[0] = centre[1];
	print(xC[0] +" " + yC[0] + " " + PI);

	// calc unit vector from x1 y1
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	Ux = Vx / norm;
	Uy = Vy / norm;

	//turn 90deg
	tmpUx = (Ux * cos(PI/2)) - (Uy * sin(PI/2));
	Uy = (Uy * cos(PI/2)) + (Ux * sin(PI/2));
	Ux = tmpUx;


	for(i=1;i<nLines;i++){
		x1n = x1 + ( Ux* (disp*i));
		x2n = x2 + ( Ux* (disp*i));
		y1n = y1 + ( Uy* (disp*i));
		y2n = y2 + ( Uy* (disp*i));
		makeLine(x1n,y1n,x2n,y2n);
		centre = calcCentre();
		xC[i] = centre[0];
		yC[i] = centre[1];
		print(xC[i] +" " + yC[i] + " " + PI);
	}

}

//----------------------------------------------------


function calcCentre(){
	intensities = getProfile();
	min = mMin(intensities);
	max = mMax(intensities);

	// invert the intensities for gaussian fitting
	for(i=0;i<lengthOf(intensities);i++){
		intensities[i] = (min - (intensities[i] - min)) + max;
	}

	// array 1,2,3...n
	pixels = newArray(intensities.length);
 	for (i=0; i<pixels.length; i++)
      		pixels[i] = i;
	
	// fit gaussian, get centre
	Fit.doFit("Gaussian",pixels, intensities);
	//Fit.plot();

	//cheack quality of the fit
	if(Fit.rSquared < 0.5){
		print("Fit to gaussian is too weak (" + Fit.rSquared + ")");
		fail = newArray(-1,-1);
		return fail;
	}
	centre = Fit.p(2);

	//calc the pixel position of the centre, follow a vector from x1,y1
	Vx = x2-x1;
	Vy = y2-y1;
	norm = sqrt( (Vx*Vx) + (Vy*Vy) );
	Ux = Vx / norm;
	Uy = Vy / norm;

	globalCentre = newArray(2);
	globalCentre[0] = x1 + (Ux * centre);
	globalCentre[1] = y1 + (Uy * centre);

	//draw centre
	setColor("red");
	drawCOval(globalCentre[0],globalCentre[1], 3,3);

	return globalCentre;
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




