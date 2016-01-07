
import ij.*;
import ij.gui.*;
import ij.io.OpenDialog;
import ij.measure.ResultsTable;
import ij.process.*;
import ij.plugin.PlugIn;
import java.io.BufferedReader;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Inserts an image or stack into a stack.
 */
public class Sigma_b implements PlugIn {

    public void run(String arg) {
        OpenDialog od = new OpenDialog("Open report file...", "");
        if (od.getFileName() == null) {
            return;
        }
        File paramFile = new File(od.getDirectory(), od.getFileName());
        try{
            BufferedReader d = new BufferedReader(new FileReader(paramFile));
            //PrintWriter pw = new PrintWriter(new FileWriter(paramFile + ".b_values.csv"), true);
            //pw.print("Coefficient,Std. Error,t");
            
            String line = d.readLine();
            IJ.write("Opened file OK\nb values:\n\n");
            ResultsTable rt = new ResultsTable();
            rt.reset();
            rt.addColumns();

            Pattern p = Pattern.compile("^b\t(\\d*.\\d*)\\s*(\\d*.\\d*)\\s*(\\d*.\\d*).*");
            Matcher m;
            String g1,g2,g3;
            double v1,v2,v3;
            
            int i = 0;
            while(line!=null){             
                m= p.matcher(line);
                if(m.matches()){
                    //IJ.write("Writing: " + line);
                    g1 = m.group(1);
                    g2 = m.group(2);
                    g3 = m.group(3);
   
                    v1 = s2d(g1);
                    v2 = s2d(g2);
                    v3 = s2d(g3);
                    
                    rt.incrementCounter();
                    rt.addValue(0,v1);
                    rt.addValue(1,v2);
                    rt.addValue(2,v3);
                    
                    i++;
                }
                line = d.readLine();         
            }
            //pw.close();
            //IJ.write("Finished..see" + paramFile + ".b_values.csv");
            
           
            for(int c = 0; c<i;c++){
               IJ.write("" + rt.getValue(0, c) + "\n"); 
            }
            
            IJ.write("\nb and coef:\n\n");
            for(int c = 0; c<i;c++){
               IJ.write("" + rt.getValue(0, c) + "\t" + rt.getValue(1, c)); 
            }
   
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    double s2d(String s) {
        Double d;
        try {
            d = new Double(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        if (d != null) {
            return (d.doubleValue());
        } else {
            return (0.0);
        }
    }
}