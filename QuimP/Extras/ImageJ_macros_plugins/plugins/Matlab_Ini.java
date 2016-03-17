
import ij.*;
import ij.plugin.PlugIn;


public class Matlab_Ini implements PlugIn{  
    
    private native void load();
    
    public void run (String arg){
        IJ.showStatus("Starting matlab.");
        IJ.write("Loading Up Matlab, please wait....");
        load();                         
        IJ.write("Complete.\n\n");
        IJ.showStatus("Matlab has started");
    }
    static { 
        //System.load("/Users/Richard/Documents/PhD/libTest/ex5/libHelloWorld.dylib");
        System.loadLibrary("MatlabLoader");  
    } 
}
