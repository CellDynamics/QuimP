
macro "Gaussian Fit [f1]" {
// get line info
getLine(x1, y1, x2, y2, lineWidth);
   if (x1==-1)
      exit("This macro requires a straight line selection");

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

//Plot.create("inverted intensities","pixel","intensity prime ", pixels, intensities);

// fit gaussian, get centre
Fit.doFit("Gaussian",pixels, intensities);
Fit.plot();

slice = getSliceNumber();

// check the quality of the fit
if(Fit.rSquared < 0.5){
	if(slice<nSlices) 
		setSlice(slice+1);
	exit("Fit to gaussian is too weak (" + Fit.rSquared + ")");
}
centre = Fit.p(2);

//calc the pixel position of the centre, follow a vector from x1,y1
Vx = x2-x1;
Vy = y2-y1;
norm = sqrt( (Vx*Vx) + (Vy*Vy) );
Ux = Vx / norm;
Uy = Vy / norm;

Cx = x1 + (Ux * centre);
Cy = y1 + (Uy * centre);

print("centre = " + centre + ", Cy= " + Cy +", Cx=" + Cx );

// output to file (read in whole file and re-write)
STRING = File.openAsString("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
F = File.open("/Users/Richard/Documents/PhD/ImageJ/bleachLocator/out.txt");
print(F,STRING);
print(F, ""+ Cx +" " + Cy + " "+ slice);

//draw centre
setColor("red");
drawCOval(Cx,Cy, 7,7);

// go to next slice
if(slice<nSlices) 
	setSlice(slice+1);

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



}




