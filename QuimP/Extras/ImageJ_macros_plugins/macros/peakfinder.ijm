macro "peak finder" {
print("\\Clear")
selectWindow("GFP3_ch00.tif");
//makeLine(258, 221, 468, 374);
elipseSel();

profile = getProfile();
print("p: "+profile[200]);

preakWidth = 12;

//for (i=0; i<profile.length; i++){
	//if( profile[i] > max) max = profile[i];
	
//}

//print(max);

peaks = newArray(profile.length);
index = newArray(profile.length);
for (i=preakWidth; i<profile.length-preakWidth; i++){
     peaks[i] = 0;
	index[i] = i;
	flag = 0;
	for (j=-preakWidth; j<=preakWidth; j++){
		if(profile[i+j] > profile[i]){
			flag = 1;
		}
	}
	if(flag==0) peaks[i] = profile[i];
}

//peaks[258] = 100;
Plot.create("peaks", "pixel","intensity", index, peaks);
Plot.setLimits(0, 258, 0, 150);
Plot.show();

    
function elipseSel(){

	List.setMeasurements;
	x = List.getValue("X");
  	y = List.getValue("Y");
  	a = List.getValue("Major");
  	b = List.getValue("Minor");
  	angle = List.getValue("Angle");
  	getVoxelSize(w, h, d, unit);
	drawEllipse(x/w, y/w, (a/w)/2, (b/w)/2, angle);
	
   
}

 function drawEllipse(x, y, a, b, angle) {
 
      beta = -angle * (PI/180);
      for (i=0; i<=360; i+=2) {
          alpha = i*(PI/180) ;
          X = x + a*cos(alpha)*cos(beta) - b*sin(alpha)*sin(beta);
          Y = y + a*cos(alpha)*sin(beta) + b*sin(alpha)*cos(beta);
          
          if (i==0) {ax1=X; ay1=Y;}
          if (i==90) {bx1=X; by1=Y;}
          if (i==180) {ax2=X; ay2=Y;}
          if (i==270) {bx2=X; by2=Y;}
      }
      makeLine(ax1, ay1, ax2, ay2);
  }

}
