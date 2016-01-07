macro "draw best fit elipse"{

// This macro draws the best fit ellipse for the current area selection.
// It uses the List.setMeasurements() and List.getValue() functions that
// were added to the macro language in ImageJ 1.42i.
  
  requires("1.42i");
  if (selectionType==-1)
      exit("Area selection required");
  List.setMeasurements;
  //print(List.getList); // list all measurements
  x = List.getValue("X");
  y = List.getValue("Y");
  a = List.getValue("Major");
  b = List.getValue("Minor");
  angle = List.getValue("Angle");
  getVoxelSize(w, h, d, unit);
  drawEllipse(x/w, y/w, (a/w)/2, (b/w)/2, angle);
  exit;

  function drawEllipse(x, y, a, b, angle) {
      autoUpdate(false);
      setLineWidth(2);
      beta = -angle * (PI/180);
      for (i=0; i<=360; i+=2) {
          alpha = i*(PI/180) ;
          X = x + a*cos(alpha)*cos(beta) - b*sin(alpha)*sin(beta);
          Y = y + a*cos(alpha)*sin(beta) + b*sin(alpha)*cos(beta);
          if (i==0) moveTo(X, Y); else lineTo(X,Y);
          if (i==0) {ax1=X; ay1=Y;}
          if (i==90) {bx1=X; by1=Y;}
          if (i==180) {ax2=X; ay2=Y;}
          if (i==270) {bx2=X; by2=Y;}
      }
      drawLine(ax1, ay1, ax2, ay2);
      drawLine(bx1, by1, bx2, by2);
      updateDisplay;
  }

}