import java.io.File;
import java.io.IOException;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.Saver;

public class ArffTest {
    static Instances instances;
    static ArffSaver saver;
    static boolean flag=true;

    public static void addData(String ticker, double price) throws IOException{
        int numAttr = instances.numAttributes(); // same for
        double[] vals = new double[numAttr];
        int i=0;
        vals[i++] = instances.attribute(0).addStringValue(ticker);
        vals[i++] = price;
        Instance instance = new Instance(1.0, vals);
        if (flag)
            saver.writeIncremental(instance);
        else
            instances.add(instance);
    }

    public static void main(String[] args) {
        if(args.length>0){
            flag=true;
        }
        FastVector atts = new FastVector();         // attributes
        atts.addElement(new Attribute("Ticker", (FastVector)null));// symbol
        atts.addElement(new Attribute("Price"));    // price that order exited at.

        instances = new Instances("Samples", atts, 0);  // create header
        saver = new ArffSaver();
        saver.setInstances(instances);
        if(flag)
            saver.setRetrieval(Saver.INCREMENTAL);

        try{
            saver.setFile(new File("test.arff"));
            addData("YY", 23.0);
            addData("XY", 24.0);
            addData("XX", 29.0);
            if(flag)
                saver.writeIncremental(null);
            else
                saver.writeBatch();
        }catch(Exception e){
            System.out.println("Exception");
        }
    }
}