macro 'Manual drift Correction' { 
   extraspace = 5; //padding the image at all sides by this value (pixels) 

   var leftClick=16, rightClick=4; 
   var shift=1, control=2, alt=8; 

   requires("1.42j"); 
   n=nSlices(); 
   if (n<2) exit ("stack required"); 
   xs=newArray(nSlices); 
   ys=newArray(nSlices); 
   for (i=0; i<n; i++) 
     xs[i] = -1; //initialize positions as unset 

   nClicked = 0; 
   i=getSliceNumber()-1; 
   flags=0; 
   lasti = -1; 
   posS = ""; 
   while (!isKeyDown("space")) { 
     getCursorLoc(x, y, z, flags); 
     if (flags&leftClick !=0) { 
       makePoint(x,y); 
       xs[i] = x; 
       ys[i] = y; 
       nClicked++; 
       lasti = i; 
       do { 
         wait(50); 
         getCursorLoc(x, y, z, flags); 
       } while (flags&leftClick !=0); 
     } 
     if (i != z) {     //slice changed 
       i = z; 
       if (xs[i]>=0) { 
         makePoint(xs[i],ys[i]); 
         lasti = i; 
       } 
       while (isKeyDown("alt")) wait(50); 
     } 
     wait(20); 
     if (lasti>=0) posS = "pos. from "+(lasti+1); 
     showStatus("','=prev, '.'=next, space=done, esc=abort. "+posS); 
   } 
   if (nClicked<2) exit("Abort - at least two positions required"); 
   lasti = -1; 
   for (i=0; i<n; i++) { 
     if (xs[i]>=0) { 
       if (lasti<0) { 
         lastx=xs[i]; 
         lasty=ys[i]; 
       } 
       for (j=lasti+1; j<i; j++) {  //interpolate 
         xs[j] = lastx + (xs[i]-lastx)*(j-lasti)/(i-lasti); 
         ys[j] = lasty + (ys[i]-lasty)*(j-lasti)/(i-lasti); 
       } 
       lastx=xs[i]; 
       lasty=ys[i]; 
       lasti=i; 
     } 
   } 
   if (lasti<n-1) { 
     for (j=lasti+1; j<n; j++) { 
       xs[j] = lastx; 
       ys[j] = lasty; 
     } 
   } 
   Array.getStatistics(xs, xmin, xmax, mean, stdDev); 
   Array.getStatistics(ys, ymin, ymax, mean, stdDev); 
   newwidth = getWidth()+(xmax-xmin)+2*extraspace; 
   newheight = getHeight() +(ymax-ymin)+2*extraspace; 
   run("Canvas Size...", "width="+ newwidth +" height="+ newheight +" position=Top-Left zero"); 
   for (i=0; i<n; i++) { 
     setSlice(i+1); 
     run("Translate...", "x="+(xmax-xs[i]+extraspace)+" y="+(ymax-ys[i]+extraspace)+" slice"); 
   } 
   showStatus("stack registered"); 

} 
