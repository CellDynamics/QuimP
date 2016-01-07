import ij.*;
import ij.process.*;
import ij.plugin.*;
import ij.io.*;
import java.io.*;
import ij.gui.*;

public class OIFROI_ implements PlugIn
{



  public void run (String arg)
  {

    int X = 0, Y = 0;
    int X1 = 0, Y1 = 0;
    int shape = 0;

    String file = selectfile ();

      IJ.write ("read file: " + file);

      try
    {
      InputStream in;		//stream to read chain
      InputStreamReader isr_in;
      String line;
      int linelength = 0;

        in = new BufferedInputStream (new FileInputStream (file));
        isr_in = new InputStreamReader (in);
      BufferedReader d = new BufferedReader (new InputStreamReader (in));

//start reading file
      for (int i = 1;; i++)
	{

	  if ((line = d.readLine ()) != null)
	    {

//we have to remove characters with char = 0 first;
	      char[] linechar = line.toCharArray ();
	      char[] linecharcopy = new char[line.length ()];

	      for (int j = 0, count = 0; j < line.length (); j++)
		{
		  if (linechar[j] != 0)
		    {
		      linecharcopy[count] = linechar[j];
		      count++;
		    }
		}

	      line = new String (linecharcopy);


	      if (line.length () > 1)
		{


		  if (line.regionMatches (0, "SHAPE=", 0, 6))
		    {
		      String shapestring = line.substring (6);
		      shapestring = shapestring.trim ();
		      shape = (int) s2d (shapestring);
		    }


if (shape == 2) { // spot ROI
		  if (line.regionMatches (0, "X=", 0, 2))
		    {
		      String xstring = line.substring (2);
		      xstring = xstring.trim ();
		      X = (int) s2d (xstring);
		    }

		  if (line.regionMatches (0, "Y=", 0, 2))
		    {
		      String ystring = line.substring (2);
		      ystring = ystring.trim ();
		      Y = (int) s2d (ystring);
		    }
}

else if (shape == 6) { // rectangular ROI
		  if (line.regionMatches (0, "X=", 0, 2))
		    {
		      String xstring = line.substring (2);
		      xstring = xstring.trim ();
			String xstrings [] = xstring.split(",");
			X=(int) s2d(xstrings[0]);
			X1=(int) s2d(xstrings[1]);
		    }

		  if (line.regionMatches (0, "Y=", 0, 2))
		    {
		      String ystring = line.substring (2);
		      ystring = ystring.trim ();

			String ystrings [] = ystring.split(",");
			Y=(int) s2d(ystrings[0]);
			Y1=(int) s2d(ystrings[1]);
		    }
}



		}



	    }
	  else
	    break;
	}

if (shape == 0) {
IJ.write("\n\n\nError: Shape of ROI not recoginzed\n");
      isr_in.close ();
      in.close ();

return;
}



      // IJ.write ("X = " + IJ.d2s (X, 0) + "\n");
      // IJ.write ("Y = " + IJ.d2s (Y, 0) + "\n");

      isr_in.close ();
      in.close ();

ImagePlus imp = WindowManager.getCurrentImage();
ImageStack stack = imp.getStack ();
int FRAMES = imp.getStackSize ();
int currentframe = imp.getCurrentSlice();
ImageWindow win = imp.getWindow ();

int width = 0;
int height = 0;

      if (shape ==2) // spot
{
      width = 7;

//       IJ.makeRectangle (X, Y, 1, 1);
IJ.makeOval(X - width / 2, Y - width / 2, width,width);
       IJ.run("Set Measurements...", "  mean redirect=None decimal=3");
       IJ.run ("Plot Z-axis Profile");

	WindowManager.setCurrentWindow(win);

      IJ.setForegroundColor (255, 255, 255);
      IJ.makeRectangle (X - width / 2-1, Y - width / 2-1, width+2, width+2);
	// IJ.makeOval(X - width / 2, Y - width / 2, width,width);
//IJ.makeRectangle (X, Y, 1, 1);
    for (int frame = 1; frame <= FRAMES; frame++)
      {
	imp.setSlice (frame);
	IJ.run ("Draw");
	}

	imp.setSlice (currentframe);

//       IJ.makeRectangle (X, Y, 1, 1);
 IJ.makeOval(X - width / 2, Y - width / 2, width,width);

}

else if (shape ==6) // rectangular ROI
{
width = X1-X;
height = Y1-Y;


IJ.makeOval(X + width / 2 -7/2, Y + height / 2 - 7/2, 7,7);
       IJ.run("Set Measurements...", "  mean redirect=None decimal=3");
       IJ.run ("Plot Z-axis Profile");

	WindowManager.setCurrentWindow(win);

      IJ.setForegroundColor (255, 255, 255);
      IJ.makeRectangle (X - 1, Y - 1, width+2, height+2);

    for (int frame = 1; frame <= FRAMES; frame++)
      {
	imp.setSlice (frame);
	IJ.run ("Draw");
	}

	imp.setSlice (currentframe);

IJ.makeOval(X + width / 2 -7/2, Y + height / 2 - 7/2, 7,7);
 // IJ.makeOval(X - width / 2, Y - width / 2, width,width);






}





    }
    catch (IOException e)
    {
      IJ.error ("Error reading file");
    }





  }


  public String selectfile ()
  {
    OpenDialog sd =
      new OpenDialog ("Read ROI from file ...", IJ.getDirectory ("image"),
		      ".roi");
    String directory = sd.getDirectory ();
    String name = sd.getFileName ();
    return (directory + name);
  }

  double s2d (String s)
  {
    Double d;
    try
    {
      d = new Double (s);
    }
    catch (NumberFormatException e)
    {
      d = null;
    }
    if (d != null)
      return (d.doubleValue ());
    else
      return (0.0);
  }

}
