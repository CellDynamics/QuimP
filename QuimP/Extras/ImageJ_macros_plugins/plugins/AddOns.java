
import ij.ImagePlus;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rtyson
 */
public class AddOns {

    public static double getFrameInterval(){
        ImagePlus IP = ij.WindowManager.getCurrentImage();

        double fi = IP.getCalibration().frameInterval;
        System.out.println("FI = " + fi);

        return fi;
    }

}
